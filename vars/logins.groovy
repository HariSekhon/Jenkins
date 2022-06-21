#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-06-20 16:59:19 +0100 (Mon, 20 Jun 2022)
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
//                                  L o g i n s
// ========================================================================== //

// Logs in to all cloud platforms for which environment variable credentials are available
//
// For more details, see:
//
//  https://github.com/HariSekhon/DevOps-Bash-tools/blob/master/login.sh

// Usage in Jenkinsfile:
//
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // run to login to any platforms for which we have standard expected environment variables available
//
//      logins()

def call(){
  echo 'Running Logins for any platforms we have environment credentials for'

  sh '''
    set -eux

    mkdir -p ~/bin

    cd ~/bin

    export PATH="$PATH:$HOME/bin:$HOME/bin/bash-tools"
    export NO_MAKE=1

    if [ -d bash-tools ]; then
      # pushd not available in sh
      cd bash-tools
      git pull
      cd ..
    else
      git clone https://github.com/HariSekhon/DevOps-Bash-tools bash-tools
    fi

    bash-tools/login.sh

  '''
}
