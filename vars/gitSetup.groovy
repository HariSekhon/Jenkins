#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-01-28 16:10:36 +0000 (Fri, 28 Jan 2022)
//
//  vim:ts=2:sts=2:sw=2:noet
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
  sh '''#!/bin/bash

    if [ -z "${GIT_EMAIL:-}" ]; then
      echo "GIT_EMAIL is not defined, please set this in Jenkinsfile environment{} section"
      exit 1
    fi

    # needed to check in
    git config --global user.name  "${GIT_USERNAME:-Jenkins}"
    git config --global user.email "$GIT_EMAIL"

    mkdir -pv ~/.ssh

    if [ -n "${DEBUG:-}" ]; then
        ssh-add -l || :
    fi

    # convenient but not secure - instead record the known hosts and make them a Jenkins secret, then source via an environment variable
    #ssh-keyscan github.com >> ~/.ssh/known_hosts
    #ssh-keyscan gitlab.com >> ~/.ssh/known_hosts
    #ssh-keyscan ssh.dev.azure.com >> ~/.ssh/known_hosts
    #ssh-keyscan bitbucket.org >> ~/.ssh/known_hosts
    # or
    #export GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no"
    # or
    #cat >> ~/.ssh/config <<EOF
#Host *
#  LogLevel DEBUG3
#  #CheckHostIP no  # used ssh-keyscan instead
#EOF
    #
    # copy from ssh-keyscan above and then hardcode here for better security:
    #echo "github.com ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==" >> ~/.ssh/known_hosts

    # if defined in Jenkinsfile environment() section
    if [ -n "${SSH_KNOWN_HOSTS:-}" ]; then
      echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
    fi

    # also better defined in Jenkinsfile environment section
    #export GIT_TRACE=1
    #export GIT_TRACE_SETUP=1
  '''
}
