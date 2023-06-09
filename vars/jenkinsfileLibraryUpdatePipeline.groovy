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

// Templated pipeline:
//
// - run from Jenkins Shared Library repo
// - enumerates all Jenkins jobs to give a choice matrix of
//   - Job Name
//   - Git Tag
// - Determines the Jenkinsfile for the given job
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
                      env: [],
                      container: null, // default or this container must have java and curl installed for Jenkins CLI
                      yamlFile: 'ci/jenkins-pod.yaml',
                      timeoutMinutes: 5
                     ] ) {

  // XXX: not working yet
  podTemplate(
    yaml: """
---
apiVersion: v1
kind: Pod
metadata:
  namespace: jenkins
  annotations:
    cluster-autoscaler.kubernetes.io/safe-to-evict: "false"
spec:
  priorityClassName: high-priority
  affinity:
    # avoid GKE preemption causing build failures
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: cloud.google.com/gke-preemptible
            operator: DoesNotExist
  # needed for argocd container to /home/jenkins/agent/workspace/<pipeline>@tmp/durable-888973f9/jenkins-log.txt, otherwise Permission Denied
  securityContext:
    runAsUser: 0
  containers:
  - name: gcloud-sdk
    image: gcr.io/google.com/cloudsdktool/cloud-sdk:latest
    tty: true
    resources:
      requests:
        cpu: 300m
        memory: 300Mi
      limits:
        cpu: "1"
        memory: 1Gi
    """
    ) {

    node {

      stage('Dynamically Populate Choices'){
        withEnv(args.env ?: []) {
          withCredentials(args.creds ?: []) {

            printEnv()
            sh 'whoami'

            // much lighter weight than having to install the CLI
            List<String> jobList = jenkins.model.Jenkins.instance.items.findAll().collect { it.name }

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

            checkout scmGit()

            echo "Getting Git Tags and Branches"

            List<String> gitTagList = gitTagList()

            List<String> gitBranchList = gitBranchList()

            List<String> gitTagsAndBranches = gitTagList + gitBranchList

            List<String> duplicates = gitTagsAndBranches.countBy{it}.grep{it.value > 1}.collect{it.key}

            if ( duplicates ) {
              echo("WARNING: duplicates detected between Git tags and branches: ${duplicates.sort().join(', ')}")
            }
          }
        }
      }
    }
  }

  pipeline {

    agent {
      kubernetes {
        defaultContainer args.container ?: error('you must specify a container and not execute in the jnlp default container as that will almost certainly fail for lack of tools and permissions')
        yamlFile args.yamlFile ?: 'ci/jenkins-pod.yaml'
      }
    }

    options {
      ansiColor('xterm')
      buildDiscarder(logRotator(numToKeepStr: '30'))
      timestamps()
      timeout (time: "${args.timeoutMinutes ?: 15}", unit: 'MINUTES')
    }

    //environment {
    //}

    parameters {
      choice(
        name: 'JOB',
        description: "Pipeline to update 'Libary(jenkins@version)'",
        choices: jobList
      )
      choice(
        name: 'GIT_REF',
        description: "Jenkins Library git tag/branch to update Jenkinsfile to use",
        choices: gitTagsAndBranchesList
      )
    }

    stages {

      stage('Environment') {
        steps {
          withEnv(args.env ?: []) {
            printEnv()
            sh 'whoami'
          }
        }
      }

      stage('Jenkins Auth Env Check') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []) {
            withCredentials(args.creds ?: []) {
              jenkinsCLICheckEnvVars()
            }
          }
        }
      }

      stage ('Setup') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          gitSetup()
          sshKnownHostsGitHub()
        }
      }

      stage('Install Packages') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []) {
            timeout (time: 5, unit: 'MINUTES') {
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
          withEnv(args.env ?: []) {
            downloadJenkinsCLI()
          }
        }
      }

      stage('Jenkins CLI Version') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []) {
            withCredentials(args.creds ?: []) {
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

      stage("Get Target Pipeline Config") {
        steps {
          script{
            String xml = jenkinsJobConfigXml(params.JOB)
            env.REPO = jenkinsJobRepo(xml)
            env.JENKINSFILE = jenkinsJobJenkinsfile(env.TARGET_JOB_XML)
          }
        }
      }

      stage("Checkout Target Pipeline Repo") {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          checkout scmGit(userRemoteConfigs: [
                              [ url: env.REPO ]
                                              ])
        }
      }

      stage('Update Target Pipeline Jenkinsfile') {
        steps {
          milestone ordinal: null, label: "Milestone: ${env.STAGE_NAME}"
          withEnv(args.env ?: []) {
            withCredentials(args.creds ?: []) {
              gitUpdateFiles(
                commit_msg: "Updated Jenkinsfile, set library tag @${params.GIT_REF}",
                commands: """
                  sed -i 's/\\(^[[:space:]]*@Library(.*@\\).*\\(.)\\)/\\1${params.GIT_REF}\\2/' "$JENKINSFILE"
                """
              )
            }
          }
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
