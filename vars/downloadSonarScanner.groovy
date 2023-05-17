//
//  Author: Hari Sekhon
//  Date: 2023-05-17 00:56:30 +0100 (Wed, 17 May 2023)
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
//                 D o w n l o a d   S o n a r S c a n n e r
// ========================================================================== //

// CLI Scanner for SonarQube
//
// Takes 2 seconds to download Sonar Scanner
//
// Takes 13 seconds to download unzip due to apt package cache updates
//
// XXX: superceded by the Jenkins SonarQube Scanner plugin
//
//      https://plugins.jenkins.io/sonar/

def call (version='4.8.0.2856') {
  String label = "Download Sonar Scanner on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 3, unit: 'MINUTES') {
      withEnv(["VERSION=$version"]) {
        installPackages(['unzip'])
        echo "$label"
        sh (
          label: "$label, version '$version'",
          script: '''
            set -eux

            echo "Downloading Sonar Scanner version $VERSION"

            curl -sSL -o "/tmp/sonar-scanner.$$.zip" "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$VERSION-linux.zip"

            cd /usr/local/

            unzip "/tmp/sonar-scanner.$$.zip"

            #latest_sonar_dir="$(ls -td sonar-scanner-* | head -n1)"

            ln -sfvT "sonar-scanner-$VERSION-linux" sonar-scanner
          '''
        )
        echo 'Adding sonar-scanner bin to $PATH'
        env.PATH += ':/usr/local/sonar-scanner/bin'
        sh (
          label: "Sonar Scanner Version",
          script: '''
            set -eu
            sonar-scanner --version
          '''
        )
      }
    }
  }
}
