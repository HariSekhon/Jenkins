#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-06-21 13:12:02 +0100 (Tue, 21 Jun 2022)
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
//                            Installs Binary to ~/bin
// ========================================================================== //

def call(Map args = [url: '', binary: '']) {
  withEnv(["URL=${args.get(url, '')}", "BINARY=${args.get(binary, '')}"]){
    sh '''
      set -eux

      mkdir -p ~/bin

      cd ~/bin

      export PATH="$PATH:$HOME/bin:$HOME/bin/bash-tools"
      export NO_MAKE=1

      # downloads the DevOps Bash tools repo which contains install_binary.sh
      curl -L https://git.io/bash-bootstrap | sh

      # this install_binary.sh script has lots of well tested logic we don't want to duplicate here
      #
      bash-tools/install_binary.sh "$URL" "$BINARY"
    '''
  }
}
