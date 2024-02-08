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

// ========================================================================== //
//                           D o w n l o a d   K I C S
// ========================================================================== //

// obsolete - Kics doesn't support downloadable binaries after 1.5.1
def call (version = '1.5.1') {
  String label = "Download KICS on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    withEnv(["VERSION=${version}"]) {
      sh (
        label: "$label",
        script: '''
          set -eux

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

          bash-tools/install/install_kics.sh
        '''
      )
      sh (
        label: "KICS Version",
        script: '''
          set -eu
          export PATH="$PATH:$HOME/bin/kics":~/bin/kics
          kics version
        '''
      )
    }
  }
}
