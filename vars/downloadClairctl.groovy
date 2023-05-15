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

// Downloading Clairctl only takes 11 seconds in testing

def call () {
  String label = "Download Clairctl on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 2, unit: 'MINUTES') {
      echo "$label"
      sh (
        label: label,
        script: '''
          set -eux
          curl -L https://raw.githubusercontent.com/jgsqware/clairctl/master/install.sh | sh
        '''
      )
      sh (
        label: "Clairctl Version",
        script: '''
          set -eu
          clairctl version
        '''
      )
    }
  }
}
