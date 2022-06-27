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
//                      D o w n l o a d   T e r r a f o r m
// ========================================================================== //

// Downloads Terraform binary to $HOME/bin if run as a user, or /usr/local/bin if run as root

// Adapted from DevOps Bash Tools setup/install_terraform.sh, install_binary.sh, install_packages.sh and lib/utils.sh

def call(version) {
  timeout(time: 5, unit: 'MINUTES') {

    installPackages(['curl', 'unzip'])

    withEnv(["VERSION=$version"]){
      sh '''
        set -eux

        # adapted from DevOps Bash tools lib/utils.sh am_root() function
        if [ "${EUID:-${UID:-$(id -u)}}" -eq 0 ]; then
          destination=/usr/local/bin
        else
          destination=~/bin
        fi

        # adapted from DevOps Bash tools lib/utils.sh get_os() and get_arch() functions
        os="$(uname -s | tr '[:upper:]' '[:lower:]')"
        arch="$(uname -m)"
        if [ "$arch" = x86_64 ]; then
          arch=amd64
        fi

        url="https://releases.hashicorp.com/terraform/$VERSION/terraform_${VERSION}_${os}_${arch}.zip"

        curl -sSLf -o terraform.zip "$url"

        unzip terraform.zip

        chmod +x terraform

        mv -v terraform "$destination/"
      '''
    }
  }
}
