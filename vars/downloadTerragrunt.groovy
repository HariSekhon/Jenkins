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
//                     D o w n l o a d   T e r r a g r u n t
// ========================================================================== //

// Downloads Terragrunt binary to $HOME/bin

// Won't overwrite an existing ~/bin/terragrunt unless the 'overwrite: true' arg is given

// Designed for Kubernetes ephemeral agents rather than old style long running agents, in which case it'd need to be modified with a unique destination per version

// Adapted from DevOps Bash Tools install/install_terragrunt.sh

// get release version from:
//
//    https://github.com/gruntwork-io/terragrunt/releases
//

def call (version) {
  String label = "Download Terragrunt on agent '$HOSTNAME'"
  if (!version) {
    error "version arg not given to downloadTerragrunt()"
  }
  // strip a 'v' prefix if present because we add it to the URL ourselves
  if (version[0] == 'v') {
    version = version.substring(1)
  }
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 5, unit: 'MINUTES') {
      withEnv(["VERSION=$version"]) {
        installBinary(
          binary: 'terragrunt',
          url: "https://github.com/gruntwork-io/terragrunt/releases/download/v$VERSION/terragrunt_{os}_{arch}"
        )
      }
      sh (
        label: "Terragrunt Version",
        script: '''
          set -eu
          export PATH="$PATH:$HOME/bin":~/bin
          terragrunt --version
        '''
      )
    }
  }
}
