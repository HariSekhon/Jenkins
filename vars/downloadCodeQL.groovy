//
//  Author: Hari Sekhon
//  Date: 2022-07-26 17:59:59 +0100 (Tue, 26 Jul 2022)
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
//                  D o w n l o a d   G i t H u b   C o d e Q L
// ========================================================================== //

// adapted from DevOps Bash tools script install/install_github_codeql.sh

def call () {
  String label = "Download CodeQL on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    sh (
      label: "$label",
      script: '''
        set -eux

        if [ -d ~/bin/codeql ]; then
            echo "CodeQL is already installed"
            exit 0
        fi

        os="$(uname -s | tr '[:upper:]' '[:lower:]')"

        if [ "$os" = darwin ]; then
            os=osx
        fi

                                  # 64 - there are no i386 or other arch available
        tarball="codeql-bundle-${os}64.tar.gz"

        #tmp="$(mktemp -d)"
        # trap signals copied from DevOps Bash tools lib/utils.sh
        #trap 'rm -fr "$tmp"' INT QUIT TRAP ABRT TERM EXIT

        # better for caching partial downloads
        tmp=/tmp

        cd "$tmp"

        curl -sSLf -o "$tarball" "https://github.com/github/codeql-action/releases/latest/download/$tarball"
        echo

        rm -fr -- ./codeql
        # the -- breaks the tar command which attempts to take it literally on GCloud SDK container
        #tar xvzf -- ./"$tarball"
        tar xvzf ./"$tarball"
        echo

        unalias rm >/dev/null 2>/dev/null || :
        unalias mv >/dev/null 2>/dev/null || :

        mv -fv -- codeql/ ~/bin/
        rm -fv -- "$tarball"
      '''
    )
    sh (
      label: "CodeQL Version",
      script: '''
        set -eu
        ~/bin/codeql/codeql version
      '''
    )
  }
}
