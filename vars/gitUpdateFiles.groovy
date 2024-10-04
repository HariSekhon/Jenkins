//
//  Author: Hari Sekhon
//  Date: 2023-06-08 22:52:03 +0100 (Thu, 08 Jun 2023)
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
//                        G i t   U p d a t e   F i l e s
// ========================================================================== //

// Updates given file(s) on a given branch in Git using the given commands, commits and pushes them
// as one atomic locked action to avoid conflicts with parallel runs of this function

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

def call (Map args = []) {

  //branch = args.branch ?: error('branch not specified in args to gitUpdateFiles()')
  branch = args.branch ?: gitCurrentBranch()
  commands = args.commands ?: error('commands not specified in args to gitUpdateFiles()')
  commit_msg = args.commit_msg ?: 'gitUpdateFiles'
  credential = args.credential ?: 'github-ssh-key'

  String label = "Git Update File(s) in branch '$branch'"

  echo "Acquiring Git Update Lock: $label"
  lock (resource: label, inversePrecedence: true) {
    // don't do this because the same pipeline may run multiple times for different repos eg. jenkinsfileLibraryUpdatePipeline.groovy
    //milestone ordinal: null, label: "Milestone: $label"
    timeout (time: 5, unit: 'MINUTES') {
      sshagent (credentials: [credential]) {
        retry (2) {
          withEnv(["BRANCH=$branch"]) {
            gitSetup()
            echo "$label"
            sh (
              label: "$label",
              script: """
                set -eux

                git status

                git fetch --all

                git checkout "$BRANCH" --force
                git pull --no-edit --no-rebase

                $commands

                git diff

                git add -A

                git status

                # the --quiet switch is required to get a non-zero exit code upon any changes
                if ! git diff-index --quiet HEAD; then
                  git commit -m "$commit_msg"

                  # XXX: push is done here and not a separate stage (which would be nicer visually in a Blue Ocean pipeline)
                  #      because we need the lock to encompass the entire operation for safety
                  git push
                fi
              """
            )
          }
        }
      }
    }
  }
}
