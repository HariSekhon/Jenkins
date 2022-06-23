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

// ========================================================================== //
//                      G i t   M e r g e   B r a n c h e s
// ========================================================================== //

def call(fromBranch, toBranch){
  String label = "Git Merge from branch '$fromBranch' to branch '$toBranch'"
  echo "Acquiring Git Merge Lock: $label"
  lock(resource: label, inversePrecedence: true) {
    milestone ordinal: null, label: "Milestone: $label"
    timeout(time: 5, unit: 'MINUTES') {
      // XXX: define this SSH private key in Jenkins -> Manage Jenkins -> Credentials as SSH username with private key
      sshagent (credentials: ['github-ssh-key']) {
        retry(2) {
          withEnv(["FROM_BRANCH=$fromBranch", "TO_BRANCH=$toBranch"]) {
            gitSetup()
            echo "$label"
            sh (
              label: "$label",
              script: '''#!/bin/bash
                set -euxo pipefail

                git status

                git fetch --all

                git checkout "$TO_BRANCH" --force
                git pull --no-edit
                git merge "origin/$FROM_BRANCH" --no-edit

                git push
              '''
            )
          }
        }
      }
    }
  }
}
