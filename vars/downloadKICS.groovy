#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-02-01 18:56:45 +0000 (Tue, 01 Feb 2022)
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

// obsolete - Kics doesn't support downloadable binaries after 1.5.1
def call(version = '1.5.1') {
  withEnv(["VERSION=${version}"]){
    sh (
      label: 'Download KICS',
      script: '''
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

        bash-tools/setup/install_kics.sh
      '''
    )
  }
}
