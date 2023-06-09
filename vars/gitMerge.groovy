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

// XXX: define 'github-ssh-key' credential (SSH private key) in Jenkins -> Manage Jenkins -> Credentials as SSH username with private key
//
//      See Also:
//
//        jenkins_cred_set_ssh_key.sh
//          or
//        jenkins_cred_cli_set_ssh_key.sh
//
//      scripts in DevOps Bash tools repo which can createthis quickly from a local private key using API or CLI
//
//        https://github.com/HariSekhon/DevOps-Bash-tools/

def call (fromBranch, toBranch, credential = 'github-ssh-key') {
  String label = "Git Merge from branch '$fromBranch' to branch '$toBranch'"
  echo "Acquiring Git Merge Lock: $label"
  lock (resource: label, inversePrecedence: true) {
    milestone ordinal: null, label: "Milestone: $label"
    timeout (time: 5, unit: 'MINUTES') {
      sshagent (credentials: [credential]) {
        retry (2) {
          withEnv(["FROM_BRANCH=$fromBranch", "TO_BRANCH=$toBranch"]) {
            gitSetup()
            echo "$label"
            sh (
              label: "$label",
              script: '''
                set -eux

                git status

                git fetch --all

                git checkout "$TO_BRANCH" --force
                git pull --no-edit --no-rebase
                git merge "origin/$FROM_BRANCH" --no-edit

                # XXX: push is done here and not a separate stage (which would be nicer visually in a Blue Ocean pipeline)
                #      because we need the lock to encompass the entire operation for safety
                git push
              '''
            )
          }
        }
      }
    }
  }
}
