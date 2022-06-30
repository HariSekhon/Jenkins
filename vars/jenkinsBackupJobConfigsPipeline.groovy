//
//  Author: Hari Sekhon
//  Date: 2022-06-30 17:22:10 +0100 (Thu, 30 Jun 2022)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/Jenkins
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//                 Jenkins Pipeline to Backup Job Configurations
// ========================================================================== //

// call this from the repo where you want to back up and commit the Jenkins job configurations to

// Usage in Jenkinsfile:
//
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // runs pipeline using Terraform 1.2.3, plans for any branch but only applies for branch 'master' with required approval, uses 'gcloud-sdk' container specified in 'ci/jenkins-pod.yaml' from the root of the repo:
//
//      jenkinsBackupJobConfigsPipeline(
//        dir: '/jobs',
//        env: ["JENKINS_USER_ID=hari.sekhon@domain.co.uk"],
//        creds: [string(credentialsId: 'hari-api-token', variable: 'JENKINS_API_TOKEN')],
//        container: 'gcloud-sdk',
//        yamlFile: 'ci/jenkins-pod.yaml'
//      )
//
//    // for explicit Git checkout settings or to prototype this pipeline call from Jenkins UI without having to push through SCM, add this parameter:
//
//       checkout: [$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[credentialsId: 'github-ssh-key', url: 'git@github.com:myorg/jenkins']] ]

def call(Map args = [
                      dir: '.',
                      checkout: [],
                      cron: 'H/10 * * * *',
                      creds: [],
                      env: [],
                      container: null, // default or this container must have java and curl installed for Jenkins CLI
                      yamlFile: 'ci/jenkins-pod.yaml',
                      timeoutMinutes: 5
                     ] ){

  pipeline {

    agent {
      kubernetes {
        defaultContainer args.container
        yamlFile args.yamlFile ?: 'ci/jenkins-pod.yaml'
      }
    }

    options {
      buildDiscarder(logRotator(numToKeepStr: '100'))
      disableConcurrentBuilds()
      timestamps()
      timeout(time: 30, unit: 'MINUTES')
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      cron("${args.get('cron', 'H/10 * * * *')}")
    }

    environment {
      DIR = "$args.dir"
      SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    }

    stages {

      stage('Environment') {
        steps {
          withEnv(args.get('env', [])){
            printEnv()
            sh 'whoami'
          }
        }
      }

      // usually not needed when called from SCM but if testing can pass checkout parameters to run this pipeline directly from Jenkins, see examples in top-level description
      stage ('Checkout') {
        when {
          expression { args.get('checkout', []) != [] }
        }
        steps {
          milestone(ordinal: null, label: "Milestone: Checkout")
          sshKnownHostsGitHub()
          checkout(args.checkout)
        }
      }

      stage ('Setup') {
        steps {
          gitSetup()
        }
      }

      stage('Auth Env Check') {
        steps {
          withEnv(args.env){
            withCredentials(args.get('creds', [])){
              jenkinsCLICheckEnvVars()
            }
          }
        }
      }

      stage('Install Packages') {
        steps {
          withEnv(args.get('env', [])){
            timeout(time: 5, unit: 'MINUTES') {
              installPackages(['java', 'curl'])
            }
          }
        }
      }

      stage('Download Jenkins CLI') {
        steps {
          withEnv(args.get('env', [])){
            downloadJenkinsCLI()
          }
        }
      }

      stage('Jenkins CLI Version') {
        steps {
          withEnv(args.get('env', [])){
            withCredentials(args.get('creds', [])){
              sh (
                label: 'Version',
                script: 'java -jar jenkins-cli.jar version'
              )
            }
          }
        }
      }

      stage('Download Jenkins Job Configurations') {
        steps {
          dir("$DIR"){
            withEnv(args.get('env', [])){
              withCredentials(args.get('creds', [])){
                jenkinsJobsDownloadConfigurations()
              }
            }
          }
        }
      }

      stage('Git Commit') {
        steps {
          dir("$DIR"){
            withEnv(args.get('env', [])){
              withCredentials(args.get('creds', [])){
                sh (
                  label: 'Git Commit',
                  script: '''
                    set -eux

                    git add -A

                    git diff

                    if ! git diff-index --quiet HEAD; then
                      git commit -m "jenkinsBackupJobsPipeline: committed Jenkins Job Configurations"
                    fi
                  '''
                )
              }
            }
          }
        }
      }

      stage('Git Push') {
        steps {
          dir("$DIR"){
            withEnv(args.get('env', [])){
              withCredentials(args.get('creds', [])){
                sh (
                  label: 'Git Push',
                  script: '''
                    set -eux
                    git push
                  '''
                )
              }
            }
          }
        }
      }

    }

    //post {
    //  failure {
    //    script {
    //      env.LOG_COMMITTERS = sh(
    //        label: 'Get Committers',
    //        script:'''
    //          git log --format='@%an' "${GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${GIT_COMMIT}" |
    //          grep -Fv -e '[bot]' -e Jenkins |
    //          sort -u |
    //          tr '\n' ' '
    //        ''',
    //        returnStdout: true
    //        ).trim()
    //    }
    //    echo "Inferred committers since last successful build via git log to be: ${env.LOG_COMMITTERS}"
    //    slackSend color: 'danger',
    //      message: "Git Merge FAILED - ${env.SLACK_MESSAGE} - @here ${env.LOG_COMMITTERS}",
    //      botUser: true
    //  }
    //  fixed {
    //    slackSend color: 'good',
    //      message: "Git Merge Fixed - ${env.SLACK_MESSAGE}",
    //      botUser: true
    //  }
    //}

  }

}
