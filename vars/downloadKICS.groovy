#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-02-01 18:56:45 +0000 (Tue, 01 Feb 2022)
//
//  vim:ts=4:sts=4:sw=4:et
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

                # downloads the DevOps Bash tools repo which contains install_binary.sh
                curl -L https://git.io/bash-bootstrap | sh

                bash-tools/setup/install_kics.sh
            '''
        )
    }
}
