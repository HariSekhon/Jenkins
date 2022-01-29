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

// Sets up Git username and email for comitting, appends SSH_KNOWN_HOSTS if set in environment{}

def call() {
  sshKnownHosts()
  sh label: 'Set up Git',
    script: '''#!/bin/bash
      set -euxo pipefail

      if [ -z "${GIT_EMAIL:-}" ]; then
        echo "GIT_EMAIL is not defined, please set this in Jenkinsfile environment{} section"
        exit 1
      fi

      # needed for 'git commit'
      git config --global user.name  "${GIT_USERNAME:-${GIT_USER:-Jenkins}}"
      git config --global user.email "$GIT_EMAIL"

      if [ -n "${DEBUG:-}" ]; then
          ssh-add -l || :
      fi

      # use sshKnownHosts instead to make the real tracked host keys available in K8s agents
      #export GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no"

      # better defined in Jenkinsfile environment{} section
      #export GIT_TRACE=1
      #export GIT_TRACE_SETUP=1
  '''
}
