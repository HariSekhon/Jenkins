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
//                          D o w n l o a d   T r i v y
// ========================================================================== //

// Downloading it for each run trades inbound bandwidth (free) for not using RAM for bigger Jenkins pods causing more scale and billable Kubernetes
//
// Downloading Trivy only takes 6 seconds in testing
//
// The alternative is using the docker image which will be cached but hold RAM for the entire duration of the pipeline, which is very RAM inefficient:
//
//    https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml

// get release version from:
//
//    https://github.com/aquasecurity/trivy/releases
//

def call (version='latest') {
  String label = "Download Trivy on agent '$HOSTNAME'"
  //  ! version instanceof String   does not work and
  //    version !instanceof String  is only available in Groovy 3
  if (version instanceof String == false) {
    error "non-string version passed to downloadTrivy() function"
  }
  if (version.contains("'")) {
    error "invalid version given to downloadTrivy(): $version"
  }
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 3, unit: 'MINUTES') {
      withEnv(["VERSION=$version"]) {
        echo "$label"
        sh (
          label: "$label, version '$VERSION'",
          script: """
            set -eux
            curl -sSfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin '$VERSION'
          """
        )
        sh (
          label: "Trivy Version",
          script: '''
            set -eu
            trivy version
          '''
        )
        label = "Download Trivy HTML Report Template"
        echo "$label"
        sh (
          label: "$label",
          script: "curl -sSfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > trivy-html.tpl"
        )
      }
    }
  }
}
