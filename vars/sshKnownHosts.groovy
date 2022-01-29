#!/usr/bin/env groovy
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

// Adds SSH Known hosts lines if not already present
//
// Can read from environment{} variable SSH_KNOWN_HOSTS or passed as an arg

def call(known_hosts='') {
  withEnv(["SSH_KNOWN_HOSTS=$known_hosts"]){
    sh label: 'Adding SSH Known Hosts',
      script: '''#!/bin/bash
        set -euxo pipefail

        # convenient but not secure - instead record the known hosts one-time and load them to dynamic K8s agents via an arg or environment variable from Jenkins secret
        #ssh-keyscan github.com >> ~/.ssh/known_hosts
        #ssh-keyscan gitlab.com >> ~/.ssh/known_hosts
        #ssh-keyscan ssh.dev.azure.com >> ~/.ssh/known_hosts
        #ssh-keyscan bitbucket.org >> ~/.ssh/known_hosts
        # or
        #cat >> ~/.ssh/config <<EOF
#Host *
#  LogLevel DEBUG3
#  #CheckHostIP no  # used ssh-keyscan instead
#EOF

        SSH_KNOWN_HOSTS_FILE="${SSH_KNOWN_HOSTS_FILE:-~/.ssh/known_hosts}"

        # if defined in Jenkinsfile environment() section
        if [ -n "${SSH_KNOWN_HOSTS:-}" ]; then
          touch "$SSH_KNOWN_HOSTS_FILE"
          while read -r line; do
            if ! grep -Fxq "$line" "$SSH_KNOWN_HOSTS_FILE"; then
              echo "$line" >> "$SSH_KNOWN_HOSTS_FILE"
            fi
          done <<< "$SSH_KNOWN_HOSTS"
        fi
    '''
  }
}
