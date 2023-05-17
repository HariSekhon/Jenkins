//
//  Author: Hari Sekhon
//  Date: 2023-05-11 05:24:55 +0100 (Thu, 11 May 2023)
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
//                          D o w n l o a d   G r y p e
// ========================================================================== //

// XXX: Jenkins plugin doesn't document how grypeScanner function can use all the Grype settings such as --scope all-layers --fail-on high
//
//    https://plugins.jenkins.io/grypescanner/

// This also gives greater control to download a specific Grype version from GitHub:
//
//    https://github.com/anchore/grype/releases

// Grype docker image doesn't have any command to keep the container alive as per:
//
//    https://github.com/anchore/grype/issues/1287
//
// and it also saves RAM and billable cloud scaling to not have that container alive the whole time
//
// Downloading Grype only takes 4 seconds in testing

def call (version='latest') {
  String label = "Download Grype on agent '$HOSTNAME'"
  //  ! version instanceof String   does not work and
  //    version !instanceof String  is only available in Groovy 3
  if (version instanceof String == false) {
    error "non-string version passed to downloadGrype() function"
  }
  if (version.contains("'")) {
    error "invalid version given to downloadGrype(): $version"
  }
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 3, unit: 'MINUTES') {
      withEnv (["VERSION=$version"]) {
        echo "$label"
        sh (
          label: "$label, version '$VERSION'",
          script: """
            set -eux
            curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin '$VERSION'
          """
        )
        sh (
          label: "Grype Version",
          script: '''
            set -eu
            grype version
          '''
        )
      }
    }
  }
}
