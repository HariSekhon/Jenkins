//
//  Author: Hari Sekhon
//  Date: 2023-05-16 02:41:44 +0100 (Tue, 16 May 2023)
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
//                       D o w n l o a d   D o c k l e
// ========================================================================== //

// Downloading Kustomize only takes a couple seconds in testing

// get release version from:
//
//    https://github.com/goodwithtech/dockle/releases
//

def call (version='0.4.11') {
  String label = "Download Dockle on agent '$HOSTNAME'"
  // strip a 'v' prefix if present because we add it to the URL ourselves
  if (version[0] == 'v') {
    version = version.substring(1)
  }
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 2, unit: 'MINUTES') {
      withEnv(["VERSION=$version"]) {
        echo "$label"
        sh (
          label: "$label, version '$version'",
          script: '''
            set -eux

            echo "Downloading Dockle version $VERSION"

            curl -sSL -o "/tmp/dockle.$$.tgz" "https://github.com/goodwithtech/dockle/releases/download/v${VERSION}/dockle_${VERSION}_Linux-64bit.tar.gz"

            tar zxvf "/tmp/dockle.$$.tgz" dockle -O > "/tmp/dockle.$$"
            chmod +x "/tmp/dockle.$$"

            mv -fv "/tmp/dockle.$$" /usr/local/bin/dockle
          '''
        )
        sh (
          label: "Dockle Version",
          script: '''
            set -eu
            dockle --version
          '''
        )
      }
    }
  }
}
