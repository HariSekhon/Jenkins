#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-06-27 13:23:20 +0100 (Mon, 27 Jun 2022)
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

// ========================================================================== //
//                     I n s t a l l   O S   P a c k a g e s
// ========================================================================== //

// Adapted from the more advanced DevOps Bash tools repo's install_packages.sh scripts

def call(packages=[]){
    sh """
        set -eux

        export DEBIAN_FRONTEND=noninteractive

        sudo=""
        # adapted from DevOps Bash tools lib/utils.sh am_root() function
        if ! [ "\${EUID:-\${UID:-\$(id -u)}}" -eq 0 ]; then
            sudo=sudo
        fi

        if type -P apt-get >/dev/null; then
            \$sudo apt-get update
            \$sudo apt-get install -y ${packages.join(' ')}
        elif type -P apk >/dev/null; then
            \$sudo apk update
            \$sudo apk add ${packages.join(' ')}
        elif type -P yum >/dev/null; then
            \$sudo yum install -y ${packages.join(' ')}
        fi
    """
}
