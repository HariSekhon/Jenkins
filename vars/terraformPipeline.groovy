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
//    // runs pipeline using Terraform 1.2.3, plans for any branch but only applies for branch 'master' with required approval, uses 'gcloud-sdk' container specified in 'ci/jenkins-pod.yaml' from the root of the repo:
//
//      terraformPipeline(version: '1.2.3',
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

def call (Map args = [
                      version: 'latest',
                      dir: '.',
                      args: '',
                      apply_branch_pattern: '^(.*/)?(main|master)$',
                      env: [],
                      creds: [],
                      checkout: [],
                      container: null,
                      yamlFile: 'ci/jenkins-pod.yaml'
                     ] ) {

  String version = args.version ?: error('Terraform version not specified')
  args.dir = args.dir ?: '.'
  String apply_branch_pattern = args.apply_branch_pattern ?: '^(.*/)?(main|master)$'
  String tfArgs = args.args ?: ''
  // env2 because env is a built-in derived from System.env
  List env2 = args.env ?: []
  List creds = args.creds ?: []
  String container = args.container ?: error('you must specify a container and not execute in the jnlp default container as that will almost certainly fail for lack of tools and permissions')
  // yamlFile is an arg to agent{ kubernetes {} } so choose a different variable name
  String yamlFilePath = args.yamlFile ?: 'ci/jenkins-pod.yaml'

  pipeline {

    agent {
      kubernetes {
        defaultContainer container
        yamlFile yamlFilePath
      }
    }

    // XXX: better to set jenkins-pod.yaml in the repo to a container with all the tooling needed
    //      using terraform's official docker image seemed smart for caching but it lacks the cloud auth tooling to be effective
    //agent {
    //  //docker {
    //  //  image "hashicorp/terraform:${version}"
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
    //            image: hashicorp/terraform:${version}
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
      buildDiscarder(logRotator(numToKeepStr: '100'))
      disableConcurrentBuilds()
      timestamps()
      timeout (time: 2, unit: 'HOURS')
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
    }

    environment {
      TERRAFORM_DIR = "$args.dir"
      TERRAFORM_VERSION = "$version"
      // https://developer.hashicorp.com/terraform/cli/config/environment-variables#tf_input
      // run non-interactively - don't prompt for any inputs
      TF_INPUT = "false"
      // don't output next command suggestions
      TF_IN_AUTOMATION = 1
      APPLY_BRANCH_PATTERN = "$apply_branch_pattern"
      /// $HOME evaluates to /home/jenkins here but /root inside gcloud-sdk container, leading to a mismatch 'no such file or directory' error
      //GOOGLE_APPLICATION_CREDENTIALS = "$HOME/.gcloud/application-credentials.json.$GIT_COMMIT"
      GOOGLE_APPLICATION_CREDENTIALS = "$WORKSPACE_TMP/.gcloud/application-credentials.json.$BUILD_TAG" // gcpSetupApplicationCredentials() will follow this path
      // to pick up downloaded Terraform binary version first
      // doesn't work if container runs as root because this evaluate to /home/jenkins/bin but inside stages you'd need /root/bin instead, and no point hacking /root/bin addition to the path because would break if container was run as any other user. Instead this is set from within the Environment stage now to be more accurate
      //PATH = "$HOME/bin:$PATH"
      //TF_LOG = "$DEBUG"
    }

    stages {

      stage('Environment') {
        steps {
          withEnv(env2) {
            sh 'whoami'
            script {
              // ${env.HOME} at script level evaluates to /home/jenkins, not that of running container
              // get accurate $HOME from running container
              home = sh (returnStdout: true, label: 'Get $HOME', script: 'echo "$HOME"').trim()
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

      stage('Auth') {
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
          withEnv(env2) {
            withCredentials(creds) {
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
          withEnv(env2) {
            timeout (time: 5, unit: 'MINUTES') {
              installPackages(['curl', 'unzip'])
            }
          }
        }
      }

      stage('Download Terraform Version') {
        steps {
          withEnv(env2) {
            downloadTerraform("$TERRAFORM_VERSION")
          }
        }
      }

      stage('Terraform Version') {
        steps {
          withEnv(env2) {
            sh 'terraform version'
          }
        }
      }

      //try {
        stage('Terraform Fmt') {
          steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
              withEnv(env2) {
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
          withEnv(env2) {
            withCredentials(creds) {
              terraformInit()
            }
          }
        }
      }

      stage('Terraform Validate') {
        steps {
          withEnv(env2) {
            terraformValidate()
          }
        }
      }

      stage('Terraform Plan') {
        steps {
          withEnv(env2) {
            withCredentials(creds) {
              terraformPlan(tfArgs)
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
              branch pattern: "$apply_branch_pattern", comparator: "REGEXP"
              // which is why we use an expression evaluation here
              expression {
                (env.GIT_BRANCH =~ ~"$apply_branch_pattern").matches()
              }
            }
          }
        }
        steps {
          //approval(args.approval_args)
          withEnv(env2) {
            approval()
          }
        }
      }

      stage('Terraform Apply') {
        when {
          allOf {
            expression {
              env.TERRAFORM_CHANGES != 'false'
            }
            anyOf {
              // XXX: branch pattern fails to match anything unless part of a multibranch pipeline
              branch pattern: "$apply_branch_pattern", comparator: "REGEXP"
              // which is why we use an expression evaluation here
              expression {
                (env.GIT_BRANCH =~ ~"$apply_branch_pattern").matches()
              }
            }
          }
        }
        steps {
          withEnv(env2) {
            withCredentials(creds) {
              terraformApply()
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
