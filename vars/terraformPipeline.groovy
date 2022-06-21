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
//                      T e r r a f o r m   P i p e l i n e
// ========================================================================== //

// Generic re-usable parameterized pipeline for running Terraform across environments

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
//      terraformPipeline(version: '1.2.3', dir: '/path/to/dir', apply_branch_pattern: 'master', env: ["GCP_SERVICEACCOUNT_KEY=${credentials('jenkins-gcp-serviceaccount-key')}"])
//
//    // with explicit checkout settings or if tried from Jenkins without SCM:
//
//      terraformPipeline(version: '1.1.7',
//                        dir: 'deployments/dev',
//                        apply_branch_pattern: 'master',
//                        checkout: [$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[credentialsId: 'github-credential', url: 'git@github.com:myorg/terraform']] ]
//                       )

def call(Map args = [
                      version: 'latest',
                      dir: '.',
                      apply_branch_pattern: '*/(main|master)$',
                      env: [],
                      checkout: []
                     ] ){

  pipeline {

    agent any

    // XXX: better to set jenkins-pod.yaml in the repo to a container with all the tooling needed
    //      using terraform's official docker image seemed smart for caching but it lacks the cloud auth tooling to be effective
    //agent {
    //  //docker {
    //  //  image "hashicorp/terraform:${args.version}"
    //  //}
    //  kubernetes {
    //    defaultContainer 'terraform'
    //    idleMinutes 5
    //    label 'terraform'
    //    yaml """\
    //      apiVersion: v1
    //      kind: Pod
    //      metadata:
    //        namespace: jenkins
    //        labels:
    //          app: terraform
    //      spec:
    //        containers:
    //          - name: terraform  # do not name this 'jnlp', without that container this'll never come up properly to execute the build
    //            image: hashicorp/terraform:${args.version}
    //            command:
    //              - cat
    //            tty: true
    //            resources:
    //              requests:
    //                cpu: 300m
    //                memory: 300Mi
    //              limits:
    //                cpu: "1"
    //                memory: 1Gi
    //        """.stripIndent()
    //   }
    //}

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
      // to pick up downloaded Terraform binary version first
      PATH = "$HOME/bin:$PATH"
      //TF_LOG = "$DEBUG"
      SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    }

    stages {

      stage('Environment') {
        steps {
          withEnv(args.get('env', [])){
            printEnv()
          }
        }
      }

      // usually not needed when called from SCM but if testing can pass checkout parameters to run this pipeline directly from Jenkins, see examples in top-level description
      //stage ('Checkout') {
      //  when {
      //    beforeAgent true
      //    expression { args.get('checkout', []) != [] }
      //  }
      //  steps {
      //    milestone(ordinal: null, label: "Milestone: Checkout")
      //    sshKnownHostsGitHub()
      //    checkout(args.checkout)
      //  }
      //}

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
        // can't inject the passed in env var before this is evaluated, ends up skipping stage
        //stages {
        //  stage('GCP Activate Service Account') {
        //    when {
        //      not {
        //        // must match the env var used in the gcpActivateServiceAccount() function
        //        environment name: 'GCP_SERVICEACCOUNT_KEY', value: ''
        //      }
        //    }
        //    steps {
        //      gcpActivateServiceAccount()
        //    }
        //  }
        //}
        steps {
          withEnv(args.get('env', [])){
            logins()
          }
        }
      }

      stage('Download Terraform Version') {
        steps {
          withEnv(args.get('env', [])){
            downloadTerraform("$TERRAFORM_VERSION")
          }
        }
      }

      stage('Terraform Version') {
        steps {
          withEnv(args.get('env', [])){
            sh 'terraform version'
          }
        }
      }

      //try {
        stage('Terraform Fmt') {
          steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
              withEnv(args.get('env', [])){
                terraformFmt()
              }
            }
          }
        }
      //} catch (Exception e) {
      //  echo e.toString()
      //}

      stage('Terraform Init') {
        steps {
          withEnv(args.get('env', [])){
            terraformInit()
          }
        }
      }

      stage('Terraform Plan') {
        steps {
          withEnv(args.get('env', [])){
            terraformPlan()
          }
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
          withEnv(args.get('env', [])){
            humanGate()
          }
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
          withEnv(args.get('env', [])){
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
