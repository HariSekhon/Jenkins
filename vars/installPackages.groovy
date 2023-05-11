//
//  Author: Hari Sekhon
//  Date: 2022-06-27 13:23:20 +0100 (Mon, 27 Jun 2022)
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
//                     I n s t a l l   O S   P a c k a g e s
// ========================================================================== //

// Adapted from the more advanced DevOps Bash tools repo's install_packages.sh and supporting scripts

def call (packages=[]) {
  String label = "Install Packages on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 5, unit: 'MINUTES') {
      withEnv(["PACKAGES=${packages.join(' ')}"]) {
        sh '''
          set -eux

          export DEBIAN_FRONTEND=noninteractive

          sudo=""
          # adapted from DevOps Bash tools lib/utils.sh am_root() function
          if ! [ "${EUID:-${UID:-$(id -u)}}" -eq 0 ]; then
            sudo=sudo
          fi

          if command -v apt-get >/dev/null; then
            $sudo apt-get update
            $sudo apt-get install -y $PACKAGES
          elif command -v apk >/dev/null; then
            $sudo apk update
            $sudo apk add $PACKAGES
          elif command -v yum >/dev/null; then
            $sudo yum install -y $PACKAGES
          else
            echo "ERROR: No recognized package manager found to install packages with"
            exit 1
          fi
        '''
      }
    }
  }
}
