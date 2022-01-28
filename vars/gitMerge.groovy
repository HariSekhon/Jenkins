//
//  Author: Hari Sekhon
//  Date: 2021-04-30 15:25:01 +0100 (Fri, 30 Apr 2021)
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

def call(fromBranch, toBranch){
  echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
  timeout(time: 1, unit: 'MINUTES') {
    sh script: 'env | sort', label: 'Environment'
  }
  String gitMergeLock = "Git Merge '$from_branch' to '$to_branch'"
  echo "Acquiring Git Merge Lock: $gitMergeLock"
  lock(resource: gitMergeLock, inversePrecedence: true) {
    milestone ordinal: 1, label: "Milestone: Git Merge '$from_branch' to '$to_branch'"
    timeout(time: 5, unit: 'MINUTES') {
      // XXX: define this SSH private key in Jenkins -> Manage Jenkins -> Credentials as SSH username with private key
      sshagent (credentials: ['github-ssh-key']) {
        retry(2) {
          withEnv(["FROM_BRANCH=$fromBranch", "TO_BRANCH=$toBranch"]) {
            gitSetup()
            sh '''#!/bin/bash
              set -euxo pipefail

              git status

              git fetch

              git checkout "$TO_BRANCH" --force
              git pull --no-edit
              git merge "origin/$FROM_BRANCH" --no-edit

              git push
            '''
          }
        }
      }
    }
  }
}
