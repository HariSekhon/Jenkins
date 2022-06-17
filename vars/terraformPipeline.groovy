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

// Usage in Jenkinsfile:
//
//    @Library('github.com/harisekhon/jenkins@master') _
//    terraformPipeline('/path/to/dir')
//

def call(Map args){

  pipeline {

    agent any


      options {
        disableConcurrentBuilds()
      }

      // backup to catch GitHub -> Jenkins webhook failures
      triggers {
        pollSCM('H/10 * * * *')
      }

      environment {
        TERRAFORM_DIR = "$args.dir"
        TF_IN_AUTOMATION = 1
        //TF_LOG = "$DEBUG"
        SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
      }

      stages {

        stage('Environment') {
          steps {
            printEnv()
          }
        }

				// see also .envrc in
        //
        //   https://github.com/HariSekhon/Terraform
        //
        // which can do this tfenv switch automatically for you
        stage('tfenv') {
          steps {
            sh '''#!/usr/bin/env bash
						set -euxo pipefail
            if [ -f .envrc ]; then
              . .envrc
            fi
            version="${TERRAFORM_VERSION:-}"
						if [ -z "$version" ]; then
								exit 0
						fi
						if ! type -P tfenv &>/dev/null; then
								exit 0
						fi
						if ! tfenv list | tfenv_list_sed | grep -Fxq "$version"; then
								echo "Terraform version '$version' not installed in tfenv, installing now..."
								tfenv install "$version"
						fi
						local current_version
						current_version="$(tfenv list | grep '^\\*' | tfenv_list_sed)"
						if [ "$current_version" != "$version" ]; then
								tfenv use "$version"
						fi
            '''
          }
        }

        // XXX: downloadTerraform if not tfenv
        // XXX: add authentication for different platforms

        stage('Terraform Version') {
          steps {
            sh 'terraform version'
          }
        }

        stage('Terraform Init') {
          steps {
            terraformInit()
          }
        }

        stage('Terraform Plan') {
          steps {
            terraformPlan()
          }
        }

        stage('Human Gate') {
          steps {
            humanGate()
          }
        }

        stage('Terraform Apply') {
          steps {
            terraformApply()
          }
        }

      }

      post {
        failure {
          script {
            env.LOG_COMMITTERS = sh(
              label: 'Get Committers',
              script:'''
                git log --format='@%an' "${GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${GIT_COMMIT}" |
                grep -Fv -e '[bot]' -e Jenkins |
                sort -u |
                tr '\n' ' '
              ''',
              returnStdout: true
              ).trim()
          }
          echo "Inferred committers since last successful build via git log to be: ${env.LOG_COMMITTERS}"
          slackSend color: 'danger',
            message: "Git Merge FAILED - ${env.SLACK_MESSAGE} - @here ${env.LOG_COMMITTERS}",
            botUser: true
        }
        fixed {
          slackSend color: 'good',
            message: "Git Merge Fixed - ${env.SLACK_MESSAGE}",
            botUser: true
        }
      }

    }

}
