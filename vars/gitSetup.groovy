//
//  Author: Hari Sekhon
//  Date: 2022-01-28 16:10:36 +0000 (Fri, 28 Jan 2022)
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
//                               G i t   S e t u p
// ========================================================================== //

// Sets up Git username and email for comitting, you may want to call one of the sshKnownHosts* functions first if using dynamic agents and Git over SSH
//
// Recommended to set an Environment variable of GIT_EMAIL=your-team@your-company.com - consider doing this at the global Jenkins level:
//
//    Manage Jenkins -> Configure System -> Global properties -> Environment Variables -> Add -> GIT_EMAIL

def call () {
  String label = "Setting up local Git repo for Jenkins"
  echo "$label"
  sh (
    label: "$label",
    script: '''
      set -eux

      GIT_USERNAME="${GIT_USERNAME:-${GIT_USER:-Jenkins}}"
      GIT_EMAIL="${GIT_EMAIL:-jenkins@noreply}"

      #if [ -z "${GIT_EMAIL:-}" ]; then
      #  echo "GIT_EMAIL is not defined, please set this in Jenkinsfile environment{} section"
      #  exit 1
      #fi

      # needed for 'git commit'
      git config user.name  "$GIT_USERNAME"
      git config user.email "$GIT_EMAIL"

      if [ -n "${DEBUG:-}" ]; then
          ssh-add -l || :
      fi

      # use sshKnownHosts() functions instead to make the real tracked host keys available in K8s agents
      #export GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no"

      # better defined in Jenkinsfile environment{} section
      #export GIT_TRACE=1
      #export GIT_TRACE_SETUP=1
    '''
  )
}
