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
//                          I n s t a l l    B i n a r y
// ========================================================================== //

// Downloads a URL, extracts the given binary from the tarball/zip and copies it to $HOME/bin

def call(Map args = [url: '', binary: '']) {
  withEnv(["URL=${args.url}", "BINARY=${args.binary}"]){
    sh '''
      set -eux

      if [ -z "$URL" ]; then
        echo "No URL passed to installBinary()"
        exit 1
      fi
      if [ -z "$BINARY" ]; then
        echo "No binary name passed to installBinary()"
        exit 1
      fi

      mkdir -p ~/bin

      cd ~/bin

      export PATH="$PATH:$HOME/bin:$HOME/bin/bash-tools"

      if [ -d bash-tools ]; then
        # pushd not available in sh
        cd bash-tools
        git pull
        cd ..
      else
        git clone https://github.com/HariSekhon/DevOps-Bash-tools bash-tools
      fi

      # this install_binary.sh script has lots of well tested logic we don't want to duplicate here
      #
      bash-tools/install_binary.sh "$URL" "$BINARY"
    '''
  }
}
