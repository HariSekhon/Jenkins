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
//        dir: 'jobs',
//        env: ["JENKINS_USER_ID=hari.sekhon@domain.co.uk", "JENKINS_CLI_ARGS=-webSocket"],
//        creds: [string(credentialsId: 'hari-api-token', variable: 'JENKINS_API_TOKEN')],
//        container: 'gcloud-sdk',
//        yamlFile: 'ci/jenkins-pod.yaml'
//      )
//
//    // for explicit Git checkout settings or to prototype this pipeline call from Jenkins UI without having to push through SCM, add this parameter:
//
//       checkout: [$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[credentialsId: 'github-ssh-key', url: 'git@github.com:myorg/jenkins']] ]

def call(Map args = [
                      jobs: [],
                      dir: '.',
                      checkout: [],
                      cron: 'H */3 * * *',
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
      cron("${args.cron ?: 'H */3 * * *'}")
    }

    environment {
      DIR = "${args.dir ?: '.'}"
    }

    stages {

      stage('Environment') {
        steps {
          withEnv(args.env ?: []){
            printEnv()
            sh 'whoami'
          }
        }
      }

      // usually not needed when called from SCM but if testing can pass checkout parameters to run this pipeline directly from Jenkins, see examples in top-level description
      stage ('Checkout') {
        when {
          expression { args.checkout }
        }
        steps {
          milestone(ordinal: null, label: "Milestone: Checkout")
          sshKnownHostsGitHub()
          checkout(args.checkout)
        }
      }

      stage ('Setup') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          gitSetup()
          sshKnownHostsGitHub()
        }
      }

      stage('Auth Env Check') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []){
            withCredentials(args.creds ?: []){
              jenkinsCLICheckEnvVars()
            }
          }
        }
      }

      stage('Install Packages') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []){
            timeout(time: 5, unit: 'MINUTES') {
              // assumes we're running on a Debian/Ubuntu based system (pretty much the standard these days)
              // including GCloud SDK's image gcr.io/google.com/cloudsdktool/cloud-sdk
              installPackages(
                [
                  'default-jdk',
                  'curl',
                  'libxml2-utils', // for xmllint
                ]
              )
            }
          }
        }
      }

      stage('Download Jenkins CLI') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []){
            downloadJenkinsCLI()
          }
        }
      }

      stage('Jenkins CLI Version') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []){
            withCredentials(args.creds ?: []){
              sh (
                label: 'Version',
                script: '''
                  set -eux
                  java -jar "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}" ${JENKINS_CLI_ARGS:-} version
                '''
              )
            }
          }
        }
      }

      stage('Download Jenkins Job Configurations') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          dir("$DIR"){
            withEnv(args.env ?: []){
              withCredentials(args.creds ?: []){
                jenkinsJobsDownloadConfigurations(jobs: args.jobs ?: [])
              }
            }
          }
        }
      }

      stage('Delete Removed Job Configurations') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          dir("$DIR"){
            withEnv(args.env ?: []){
              withCredentials(args.creds ?: []){
                jenkinsDeleteRemovedJobXmls()
              }
            }
          }
        }
      }

      stage('Git Commit') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          dir("$DIR"){
            withEnv(args.env ?: []){
              withCredentials(args.creds ?: []){
                sh (
                  label: 'Git Commit',
                  script: '''
                    set -eux

                    git add .

                    git diff

                    git status

                    if ! git diff-index --quiet HEAD; then
                      git commit -m "$JOB_NAME: committed Jenkins Job Configurations"
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
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          dir("$DIR"){
            withEnv(args.env ?: []){
              withCredentials(args.creds ?: []){
                // XXX: define this SSH private key in Jenkins -> Manage Jenkins -> Credentials as SSH username with private key
                sshagent (credentials: ['github-ssh-key']) {
                  sh (
                    label: 'Git Push',
                    script: '''
                      set -eux

                      branch="${GIT_BRANCH#origin/}"

                      # do a local merge rather than fail if there are new edits in the branch
                      git pull origin "$branch" --no-edit

                      git push origin HEAD:"$branch"
                    '''
                  )
                }
              }
            }
          }
        }
      }

    }

    post {
      failure {
        Notify()
      }
      fixed {
        Notify()
      }
    }

  }

}
