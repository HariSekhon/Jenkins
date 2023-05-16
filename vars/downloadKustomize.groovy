//
//  Author: Hari Sekhon
//  Date: 2021-09-01 11:57:48 +0100 (Wed, 01 Sep 2021)
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
//                      D o w n l o a d   K u s t o m i z e
// ========================================================================== //

// Downloading Kustomize only takes 1 second in testing
//
// This is better (easier and more version flexible across different pipelines) than maintaining a docker image harisekhon/git-kustomize docker image.
// The docker image cache is of negligible benefit in this case
//
// See top-level Jenkinsfile, adjacent gitKustomizeImage.groovy and jenkins-agent-pod.yaml in:
//
//    https://github.com/HariSekhon/Kubernetes-configs

// get release version from:
//
//    https://github.com/kubernetes-sigs/kustomize/releases
//

// Kustomize version needs to be fairly recent to solve 'unknown field "includeCRDs"' when combining Kustomize + Helm with includeCRDs option as seen in *-kustomization.yaml in https://github.com/HariSekhon/Kubernetes-configs
def call (version='4.5.7') {
  String label = "Download Kustomize on agent '$HOSTNAME'"
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

            echo "Downloading Kustomize version $VERSION"

            curl -sSL -o "/tmp/kustomize.$$.tgz" "https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize%2Fv${VERSION}/kustomize_v${VERSION}_linux_amd64.tar.gz"

            tar zxvf "/tmp/kustomize.$$.tgz" kustomize -O > "/tmp/kustomize.$$"
            chmod +x "/tmp/kustomize.$$"

            mv -fv "/tmp/kustomize.$$" /usr/local/bin/kustomize
          '''
        )
        sh (
          label: "Kustomize Version",
          script: '''
            set -eu
            kustomize version
          '''
        )
      }
    }
  }
}
