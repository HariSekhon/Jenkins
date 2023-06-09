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

def call (Map args = [
                      jobs: [],
                      dir: '.',
                      checkout: [],
                      cron: 'H */3 * * *',
                      creds: [],
                      env: [],
                      container: null, // default or this container must have java and curl installed for Jenkins CLI
                      yamlFile: 'ci/jenkins-pod.yaml',
                      timeoutMinutes: 5
                     ] ) {

  echo "Getting Jenkins Jobs"

  List<String> jenkinsJobList = jenkinsJobList()

  echo "Getting Git Tags and Branches"

  List<String> gitTagList = gitTagList()

  List<String> gitBranchList = gitBranchList()

  List<String> gitTagsAndBranches = gitTagList + gitBranchList

  List<String> duplicates = gitTagsAndBranches.countBy{it}.grep{it.value > 1}.collect{it.key}

  if ( duplicates ) {
    echo("WARNING: duplicates detected between Git tags and branches: ${duplicates.sort().join(', ')}")
  }

  pipeline {

    agent {
      kubernetes {
        defaultContainer 'gcloud-sdk'
        yamlFile "ci/jenkins-pod.yaml"
      }
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
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
				choices: jenkinsJobList
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
