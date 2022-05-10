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

// There is now a harisekhon/git-kustomize docker image to avoid re-downloading kustomize for every pipeline run and reuse docker image caching
//
// See top-level Jenkinsfile, adjacent gitKustomizeImage.groovy and jenkins-agent-pod.yaml in:
//
//    https://github.com/HariSekhon/Kubernetes-configs

def call(version='4.3.0'){
  timeout(time: 2, unit: 'MINUTES') {
    withEnv(["VERSION=$version"]) {
      String label = "Download Kustomize version $version"
      echo "$label"
      sh (
        label: "$label",
        script: '''#!/bin/bash
          set -euxo pipefail
          echo "Downloading Kustomize version $VERSION"
          curl -sSL -o /tmp/kustomize.$$.tgz https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize%2Fv${VERSION}/kustomize_v${VERSION}_linux_amd64.tar.gz
          tar zxvf /tmp/kustomize.$$.tgz kustomize -O > /tmp/kustomize.$$
          chmod +x /tmp/kustomize.$$
          mv -iv /tmp/kustomize.$$ /usr/local/bin/kustomize
        '''
      )
    }
  }
}
