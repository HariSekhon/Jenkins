//
//  Author: Hari Sekhon
//  Date: 2022-01-06 17:35:16 +0000 (Thu, 06 Jan 2022)
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
//                     T e r r a g r u n t   P i p e l i n e
// ========================================================================== //

// Generic re-usable parameterized pipeline for running Terragrunt across environments

// Usage in Jenkinsfile:
//
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // runs pipeline using Terragrunt 1.2.3, plans for any branch but only applies for branch 'master' with required approval, uses 'gcloud-sdk' container specified in 'ci/jenkins-pod.yaml' from the root of the repo:
//
//      terragruntPipeline(version: '1.2.3',
//                        dir: '/path/to/dir',
//                        apply_branch_pattern: 'master',
//                        creds: [string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')],
//                        container: 'gcloud-sdk',
//                        yamlFile: 'ci/jenkins-pod.yaml')
//
//    // to only execute if files under the /path/to/deployment/dir and /path/to/modules have changed, under SCM using Git add Additional Behaviours -> Polling ignores commits in certain paths -> Include box, add your patterns there
//
//    // for explicit Git checkout settings or to prototype this pipeline call from Jenkins UI without having to push through SCM, add this parameter:
//
//       checkout: [$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[credentialsId: 'github-ssh-key', url: 'git@github.com:myorg/terraform']] ]

def call(Map args = [
                      version: 'latest',
                      dir: '.',
                      args: '',
                      apply_branch_pattern: '^(.*/)?(main|master)$',
                      env: [],
                      creds: [],
                      checkout: [],
                      container: null,
                      yamlFile: 'ci/jenkins-pod.yaml'
                     ] ){

  args.version ?: error('Terragrunt version not specified')
  args.dir = args.dir ?: '.'
  args.apply_branch_pattern = args.apply_branch_pattern ?: '^(.*/)?(main|master)$'
  args.args = args.args ?: ''
  args.env = args.env ?: []
  args.creds = args.creds ?: []

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
      timeout(time: 2, unit: 'HOURS')
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
    }

    environment {
      TERRAFORM_DIR = "$args.dir"
      TERRAFORM_VERSION = "$args.version"
      TF_IN_AUTOMATION = 1
      APPLY_BRANCH_PATTERN = "${args.apply_branch_pattern}"
      GOOGLE_APPLICATION_CREDENTIALS = "$WORKSPACE_TMP/.gcloud/application-credentials.json.$BUILD_TAG" // gcpSetupApplicationCredentials() will follow this path
      //TF_LOG = "$DEBUG"
      SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    }

    stages {

      stage('Environment') {
        steps {
          withEnv(args.env){
            sh 'whoami'
            script {
              // ${env.HOME} at script level evaluates to /home/jenkins, not that of running container
              // get accurate $HOME from running container
              home = sh(returnStdout: true, label: 'Get $HOME', script: 'echo "$HOME"').trim()
              echo "Setting PATH to include $home/bin"
              env.PATH = "$home/bin:${env.PATH}"
            }
            printEnv()
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

      stage('Auth') {
        steps {
          withEnv(args.env){
            withCredentials(args.creds){
              // tries everything
              login()
              // or call something simpler if you know what environment you're executing in
              //gcpSetupApplicationCredentials()
            }
          }
        }
      }

      stage('Install Packages') {
        steps {
          withEnv(args.env){
            timeout(time: 5, unit: 'MINUTES') {
              installPackages(['curl', 'unzip'])
            }
          }
        }
      }

      stage('Download Terragrunt Version') {
        steps {
          withEnv(args.env){
            downloadTerragrunt("$TERRAFORM_VERSION")
          }
        }
      }

      stage('Terragrunt Version') {
        steps {
          withEnv(args.env){
            sh 'terragrunt version'
          }
        }
      }

      //try {
        stage('Terragrunt Fmt') {
          steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
              withEnv(args.env){
                terragruntFmt()
              }
            }
          }
        }
      //} catch (Exception e) {
      //  echo e.toString()
      //}

      stage('Terragrunt Init') {
        steps {
          withEnv(args.env){
            withCredentials(args.creds){
              terragruntInit()
            }
          }
        }
      }

      stage('Terragrunt Validate') {
        steps {
          withEnv(args.env){
            terragruntValidate()
          }
        }
      }

      stage('Terragrunt Plan') {
        steps {
          withEnv(args.env){
            withCredentials(args.creds){
              terragruntPlan(args.args)
            }
          }
        }
      }

      stage('Approval') {
        when {
          beforeInput true
          allOf {
            expression {
              env.TERRAFORM_CHANGES != 'false'
            }
            anyOf {
              // XXX: branch pattern fails to match anything unless part of a multibranch pipeline
              branch pattern: "$args.apply_branch_pattern", comparator: "REGEXP"
              // which is why we use an expression evaluation here
              expression {
                (env.GIT_BRANCH =~ ~"$args.apply_branch_pattern").matches()
              }
            }
          }
        }
        steps {
          //approval(args.approval_args)
          withEnv(args.env){
            approval()
          }
        }
      }

      stage('Terragrunt Apply') {
        when {
          allOf {
            expression {
              env.TERRAFORM_CHANGES != 'false'
            }
            anyOf {
              // XXX: branch pattern fails to match anything unless part of a multibranch pipeline
              branch pattern: "$args.apply_branch_pattern", comparator: "REGEXP"
              // which is why we use an expression evaluation here
              expression {
                (env.GIT_BRANCH =~ ~"$args.apply_branch_pattern").matches()
              }
            }
          }
        }
        steps {
          withEnv(args.env){
            withCredentials(args.creds){
              terragruntApply(args.args)
            }
          }
        }
      }

    }

    post {
      failure {
        script {
          env.LOG_COMMITTERS = gitLogBrokenCommitters()
        }
        slackSend color: 'danger',
          message: "Terragrunt Job FAILED - ${env.SLACK_MESSAGE} - @here ${env.LOG_COMMITTERS}",
          botUser: true
      }
      fixed {
        slackSend color: 'good',
          message: "Terragrunt Job Fixed - ${env.SLACK_MESSAGE}",
          botUser: true
      }
    }

  }

}
