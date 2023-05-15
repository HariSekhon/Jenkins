//
//  Author: Hari Sekhon
//  Date: 2023-05-15 05:21:13 +0100 (Mon, 15 May 2023)
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
//                       D o w n l o a d   C l a i r c t l
// ========================================================================== //

// Downloading Clairctl only takes 4 seconds in testing

def call (version='4.6.1') {
  String label = "Download Clairctl on agent '$HOSTNAME'"
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
          label: label,
          script: '''
            set -eux
            curl -sSL -o /tmp/clairctl.$$ "https://github.com/quay/clair/releases/download/v$VERSION/clairctl-linux-amd64"
            chmod +x /tmp/clairctl.$$
            mv -vf /tmp/clairctl.$$ /usr/local/bin/clairctl
          '''
        )
        sh (
          label: "Clairctl Version",
          script: '''
            set -eu
            clairctl --version
          '''
        )
      }
    }
  }
}
