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
//                     D o w n l o a d   A r g o C D   C L I
// ========================================================================== //

// Downloading it for each run trades inbound bandwidth (free) for not using RAM for bigger Jenkins pods causing more scale and billable Kubernetes
//
// Downloading ArgoCD CLI only takes 2 seconds in testing
//
// The alternative is using the docker image which will be cached but hold RAM for the entire duration of the pipeline, which is very RAM inefficient:
//
//    https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml

// get release version from:
//
//    https://github.com/argoproj/argo-cd/releases
//

def call (version='latest') {
  String label = "Download ArgoCD CLI on agent '$HOSTNAME'"
  //  ! version instanceof String   does not work and
  //    version !instanceof String  is only available in Groovy 3
  if (version instanceof String == false) {
    error "non-string version passed to downloadArgoCD CLI() function"
  }
  if (version.contains("'")) {
    error "invalid version given to downloadArgo(): $version"
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
            os="\$(uname -s | tr '[:upper:]' '[:lower:]')"
            arch="\$(uname -m)"
            if [ "\$arch" = x86_64 ]; then
              arch=amd64
            fi
            tmpfile="/tmp/argocd.\$\$"
            if [ "$VERSION" = latest ]; then
              curl -sSL -o "\$tmpfile" "https://github.com/argoproj/argo-cd/releases/latest/download/argocd-\$os-\$arch"
              chmod 0555 "\$tmpfile"
              unalias mv 2>/dev/null || :
              mv -fv "\$tmpfile" /usr/local/bin/argocd
            else
              curl -sSL -o "\$tmpfile" "https://github.com/argoproj/argo-cd/releases/download/$VERSION/argocd-\$os-\$arch"
              chmod 0555 "\$tmpfile"
              unalias mv 2>/dev/null || :
              mv -fv "\$tmpfile" /usr/local/bin/argocd
            fi
          """
        )
        sh (
          label: "ArgoCD Version",
          script: '''
            set -eu
            argocd version --client
          '''
        )
      }
    }
  }
}
