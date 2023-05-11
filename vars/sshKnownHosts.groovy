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
//                         S S H   K n o w n   H o s t s
// ========================================================================== //

// Adds SSH Known hosts lines if not already present
//
// Can read from environment{} variable SSH_KNOWN_HOSTS or passed as an arg

def call (knownHosts='', name='') {
  withEnv(["SSH_KNOWN_HOSTS=$knownHosts"]) {
    // only works on stages, not steps
    //when {
    //  not { environment name: 'SSH_KNOWN_HOSTS', value: '' }
    //}
    String label = "Adding SSH Known Hosts: $name"
    sh (
      label: "$label",
      script: '''#!/usr/bin/env bash
        set -euxo pipefail

        # convenient and dynamic but not secure:
        #
        #   ssh-keyscan github.com >> ~/.ssh/known_hosts
        #   ssh-keyscan gitlab.com >> ~/.ssh/known_hosts
        #   ssh-keyscan ssh.dev.azure.com >> ~/.ssh/known_hosts
        #   ssh-keyscan bitbucket.org >> ~/.ssh/known_hosts
        #
        # instead load them from revision controlled adjacent functions:
        #
        #   sshKnownHostsGitHub()
        #   sshKnownHostsGitLab()
        #   sshKnownHostsBitbucket()
        #   sshKnownHostsAzureDevOps()
        #
        # or if you want them more dynamic you can load them via an arg to this function or environment variable 'SSH_KNOWN_HOSTS' from a Jenkins secret to share across all pipelines

        # don't do this either
        #cat >> ~/.ssh/config <<EOF
#Host *
#  LogLevel DEBUG3
#  #CheckHostIP no  # used ssh-keyscan instead
#EOF

        SSH_KNOWN_HOSTS_FILE="${SSH_KNOWN_HOSTS_FILE:-${HOME:-$(cd && pwd)}/.ssh/known_hosts}"

        # if defined in Jenkinsfile environment() section
        if [ -n "${SSH_KNOWN_HOSTS:-}" ]; then
          mkdir -pv "${SSH_KNOWN_HOSTS_FILE%/*}"
          touch "$SSH_KNOWN_HOSTS_FILE"
          while read -r line; do
            if ! grep -Fxq "$line" "$SSH_KNOWN_HOSTS_FILE"; then
              echo "$line" >> "$SSH_KNOWN_HOSTS_FILE"
            fi
          done <<< "$SSH_KNOWN_HOSTS"
        fi
      '''
      //sshKnownHostsGitHub()
      //sshKnownHostsGitLab()
      //sshKnownHostsBitbucket()
      //sshKnownHostsAzureDevOps()
    )
  }
}
