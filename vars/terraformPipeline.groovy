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
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // run using Terraform 1.2.3, plan for any branch but only apply for branch 'master':
//
//      terraformPipeline(version: '1.2.3', dir: '/path/to/dir', apply_branch_pattern: 'master')
//
//    // with credentials to your cloud infrastructure:
//
//      terraformPipeline(version: '1.2.3', dir: '/path/to/dir', apply_branch_pattern: 'master', withEnv: ["GCP_SERVICEACCOUNT_KEY=${credentials('jenkins-gcp-serviceaccount-key')}"])
//

def call(Map args = [version: 'latest', dir: '.', apply_branch_pattern: '*/(main|master)$', withEnv: [], checkout: [] ] ){

  pipeline {

    agent {
      //docker {
      //  image "hashicorp/terraform:${args.version}"
      //}
      kubernetes {
        defaultContainer 'terraform'
        idleMinutes 5
        label 'terraform'
        yaml """\
          apiVersion: v1
          kind: Pod
          metadata:
            namespace: jenkins
            labels:
              app: terraform
          spec:
            containers:
              - name: gcloud-sdk  # do not name this 'jnlp', without that container this'll never come up properly to execute the build
                image: hashicorp/terraform:${args.version}
                tty: true
                resources:
                  requests:
                    cpu: 300m
                    memory: 300Mi
                  limits:
                    cpu: "1"
                    memory: 1Gi
            """.stripIndent()
       }
    }

    options {
      disableConcurrentBuilds()
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
    }

    environment {
      TERRAFORM_DIR = "$args.dir"
      TERRAFORM_VERSION = "$args.version"
      TF_IN_AUTOMATION = 1
      //TF_LOG = "$DEBUG"
      SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    }

    stages {

      stage('Environment') {
        withEnv(withEnv){
          steps {
            printEnv()
          }
        }
      }

      stage ('Checkout') {
        when {
          beforeAgent true
          expression { checkout != [] }
        }
        steps {
          milestone(ordinal: 10, label: "Milestone: Checkout")
          checkout(checkout)
        }
      }

      // done via more cachable hashicorp/terraform image now
      //
      // see also .envrc in
      //
      //   https://github.com/HariSekhon/Terraform
      //
      //stage('tfenv') {
      //  steps {
      //    sh '''#!/usr/bin/env bash
      //    set -euxo pipefail
      //    if [ -f .envrc ]; then
      //      . .envrc
      //    fi
      //    version="${TERRAFORM_VERSION:-}"
      //    if [ -z "$version" ]; then
      //        exit 0
      //    fi
      //    if ! type -P tfenv &>/dev/null; then
      //        exit 0
      //    fi
      //    if ! tfenv list | tfenv_list_sed | grep -Fxq "$version"; then
      //        echo "Terraform version '$version' not installed in tfenv, installing now..."
      //        tfenv install "$version"
      //    fi
      //    local current_version
      //    current_version="$(tfenv list | grep '^\\*' | tfenv_list_sed)"
      //    if [ "$current_version" != "$version" ]; then
      //        tfenv use "$version"
      //    fi
      //    '''
      //  }
      //}

      stage('Logins') {
        steps {
          logins()
        }
      }

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
        when {
          beforeAgent true
          // TODO: test with and without
          // https://www.jenkins.io/doc/book/pipeline/syntax/#evaluating-when-before-the-input-directive
          beforeInput true  // change order to evaluate when{} first to only prompt if this is on production branch
          branch pattern: "$args.apply_branch_pattern"
        }
        steps {
          //humanGate(args.human_gate_args)
          humanGate()
        }
      }

      stage('Terraform Apply') {
        when {
          beforeAgent true
          // TODO: test with and without
          // https://www.jenkins.io/doc/book/pipeline/syntax/#evaluating-when-before-the-input-directive
          beforeInput true  // change order to evaluate when{} first to only prompt if this is on production branch
          branch pattern: "$args.apply_branch_pattern"
        }
        steps {
          echo "Applying"
          //terraformApply()
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
