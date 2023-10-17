//
//  Author: Hari Sekhon
//  Date: 2023-06-08 22:10:23 +0100 (Thu, 08 Jun 2023)
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
//              J e n k i n s f i l e   L i b r a r y   U p d a t e
// ========================================================================== //

// Perhaps too clever for my own good, this is not simple, but it is impressively dynamic
//
// Choices are generated dynamically via node{} scripted pipeline, and then fed to a declarative pipeline{}
//
// Choices are remembered after first run and subsequently Build with Parameters prompt is then available
//
// XXX: The first run will populate the choices but continues executing assuming the default first choices for job and git ref
//      so the pipeline checks the build number and aborts if BUILD_NUMBER=1 for safety
//      The second run will force a "Build with Parameters" pop-up choice to the calling user

// Templated pipeline:
//
// - run from Jenkins Shared Library repo
// - enumerates all Jenkins jobs to give a choice matrix of
//   - Job Name
//   - Git Tags / Branches
// - Fetches the given job's XML config and determines
//    - the repo for the given job
//    - the branch for the given job
//    - the Jenkinsfile for the given job
// - Checks out the Git repo containing the Jenkinsfile on the branch it watches
// - Updates the Jenkinsfile Library("somelib@version") reference with the selected Git tag
// - Commits & Pushes the Jenkinsfiles
// - May need a GitHub branch protection exception for the Jenkins CI github user

// Usage:
//
//    jenkinsfileLibraryUpdatePipeline(
//        env: [
//            "JENKINS_USER_ID=hari@domain.com",
//            "JENKINS_CLI_ARGS=-webSocket"
//        ],
//        creds: [
//            string(credentialsId: 'job-api-token', variable: 'JENKINS_API_TOKEN')
//        ],
//        // without specifying the container you'll get an error like this by executing in the jnlp container:
//        //
//        //      /home/jenkins/agent/workspace/test@tmp/durable-36843a52/script.sh: 13: /home/jenkins/agent/workspace/test@tmp/durable-36843a52/script.sh: sudo: not found
//        container: 'gcloud-sdk',
//        yamlFile: 'ci/jenkins-pod.yaml'
//    )

def call (Map args = [
                      creds: [],
                      github_ssh_key_credential_id: 'github-ssh-key', // set this to the Jenkins credential id of the private key for checking out private repos
                      env: [],
                      container: null, // default or this container must have java and curl installed for Jenkins CLI
                      yamlFile: 'ci/jenkins-pod.yaml',
                      timeoutMinutes: 5
                     ] ) {

  List creds = args.creds ?: []
  // env2 because env is a built-in derived from System.env
  List env2 = args.env ?: []
  String container = args.container ?: error('you must specify a container and not execute in the jnlp default container as that will almost certainly fail for lack of tools and permissions')
  // yamlFile is an arg to agent{ kubernetes {} } so choose a different variable name
  String yamlFilePath = args.yamlFile ?: 'ci/jenkins-pod.yaml'
  String timeoutMinutes = "${args.timeoutMinutes ?: 15}"

  // must be here so that these variables can be scoped from node{} to pipeline{}
  List<String> jobList = []
  List<String> gitTagsAndBranchesList = []

  // Scripted Pipelines have horrible Kubernetes support, can't use yamlFile and the podTemplate doesn't work properly
  // so avoid using a full fledged gcloud-sdk container and just make do with a minimal dependencies jenkinsJobListAPI()
  // without installing anything
  node {
    stage('Dynamically Populate Choices') {
      withEnv(env2) {
        withCredentials(creds) {

          printEnv()
          sh 'whoami'

          // much lighter weight than having to install the CLI
          jobList = jenkinsJobListAPI()

          //jenkinsCLICheckEnvVars()
          //timeout (time: 5, unit: 'MINUTES') {
          //  // assumes we're running on a Debian/Ubuntu based system (pretty much the standard these days)
          //  // including GCloud SDK's image gcr.io/google.com/cloudsdktool/cloud-sdk
          //  installPackages(
          //    [
          //      'default-jdk',
          //      'curl',
          //      'libxml2-utils', // for xmllint
          //    ]
          //  )
          //}
          //downloadJenkinsCLI()
          //
          //echo "Getting Jenkins Jobs"
          //
          //List<String> jobList = jenkinsJobList()

          checkout scm

          echo "Getting Git Tags and Branches"

          List<String> gitTagList = gitTagList()

          List<String> gitBranchList = gitBranchList()

          gitTagsAndBranchesList = gitTagList + gitBranchList

          //  grep needs to be approved, but would also require @NonCPS which probably isn't worth it for this
          //List<String> duplicates = gitTagsAndBranchesList.countBy { it }.grep { it.value > 1 }.collect { it.key }
          //List<String> duplicates = gitTagsAndBranchesList.countBy { it }.grep { it.value > 1 }*.key

          //if ( duplicates ) {
          //  echo("WARNING: duplicates detected between Git tags and branches: ${duplicates.sort().join(', ')}")
          //}
        }
      }
    }
  }

  pipeline {

    agent {
      kubernetes {
        defaultContainer container
        yamlFile yamlFilePath
      }
    }

    options {
      ansiColor('xterm')
      buildDiscarder(logRotator(numToKeepStr: '30'))
      timestamps()
      timeout (time: timeoutMinutes, unit: 'MINUTES')
    }

    //environment {
    //}

    parameters {
      choice(
        name: 'JOB',
        description: "Pipeline to update 'Library(jenkins@version)'",
        choices: jobList
      )
      choice(
        name: 'GIT_REF',
        description: "Jenkins Library git tag/branch to update Jenkinsfile to use",
        choices: gitTagsAndBranchesList
      )
      booleanParam(
        name: 'BUILD',
        description: 'Build this pipeline after updating its Jenkinsfile?',
        defaultValue: false
      )
    }

    stages {

      stage('Setup') {
        parallel {
          stage('Environment') {
            steps {
              withEnv(env2) {
                printEnv()
                sh 'whoami'
              }
            }
          }

          stage ('Git Setup') {
            steps {
              script {
                //if ( ! args.github_ssh_key_credential_id ) {
                //  error("github_ssh_key_credential_id parameter was not set when calling jenkinsfileLibraryUpdatePipeline()")
                //}
                args.github_ssh_key_credential_id = args.github_ssh_key_credential_id ?: 'github-ssh-key'
              }
              gitSetup()
              sshKnownHostsGitHub()
            }
          }

          stage('Install Packages') {
            steps {
              withEnv(env2) {
                timeout (time: 5, unit: 'MINUTES') {
                  // assumes we're running on a Debian/Ubuntu based system (pretty much the standard these days)
                  // including GCloud SDK's image gcr.io/google.com/cloudsdktool/cloud-sdk
                  installPackages(
                    [
                      'default-jdk',
                      'curl',
                      //'libxml2-utils', // for xmllint - not using in this pipeline
                    ]
                  )
                }
              }
            }
          }
        }
      }

      stage('Jenkins CLI Setup') {
        parallel {
          stage('Download Jenkins CLI') {
            steps {
              withEnv(env2) {
                downloadJenkinsCLI()
              }
            }
          }
          stage('Jenkins Auth Env Check') {
            steps {
              withEnv(env2) {
                withCredentials(creds) {
                  jenkinsCLICheckEnvVars()
                }
              }
            }
          }
        }
      }

      stage('Jenkins CLI Version') {
        steps {
          withEnv(env2) {
            withCredentials(creds) {
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

      stage("Get Pipeline Config") {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}, Job: ${params.JOB}"
          script {
            withEnv(env2) {
              withCredentials(creds) {
                String xml = jenkinsJobConfigXml(params.JOB)
                env.REPO = jenkinsJobRepo(xml)
                env.BRANCH = jenkinsJobBranch(xml)
                env.JENKINSFILE = jenkinsJobJenkinsfile(xml)
              }
            }
          }
        }
      }

      stage("Checkout Pipeline Repo") {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}, Job: ${params.JOB}"
          withEnv(env2) {
            withCredentials(creds) {
              checkout (
                [
                  $class: 'GitSCM',
                  userRemoteConfigs: [
                    [
                      url: env.REPO,
                      // without setting credential:
                      //
                      //    hudson.plugins.git.GitException: Failed to fetch from git@github.com:
                      //
                      // but be careful not to set credentialsId to args.credentials array, otherwise:
                      //
                      // org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: No such field found: field org.jenkinsci.plugins.workflow.cps.UninstantiatedDescribableWithInterpolation credentialsId
                      //
                      credentialsId: args.github_ssh_key_credential_id
                    ]
                  ],
                  branches: [
                    [
                      name: env.BRANCH
                    ]
                  ]
                ]
              )
            }
          }
        }
      }

      stage('Update Pipeline Jenkinsfile') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}, Job: ${params.JOB}"
          withEnv(env2) {
            withCredentials(creds) {
              // protection because the first run of the pipeline will just assume to take the first choices of both the job name and the git ref - second run will force Build with Parameters pop-up choice
              script {
                if ( env.BUILD_NUMBER == 1 ) {
                  echo "First run of pipeline - aborting for safety as it will have inherited the default first parameter choices and we can't be sure this should actually be actioned"
                  exit 1
                }
              }
              gitUpdateFiles(
                branch: env.BRANCH,
                commit_msg: "Updated Jenkinsfile, set library tag @${params.GIT_REF}",
                commands: """
                  sed -i 's/\\(^[[:space:]]*@Library(.*@\\).*\\(.)\\)/\\1${params.GIT_REF}\\2/' "$JENKINSFILE"
                """
              )
            }
          }
        }
      }

      stage('Build Job') {
        when {
          expression { params.BUILD == true }
        }
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          build job: params.JOB, wait: true
        }
      }

    }

    //post {
    //  failure {
    //    Notify()
    //  }
    //  fixed {
    //    Notify()
    //  }
    //}

  }

}
