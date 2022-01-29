//
//  Author: Hari Sekhon
//  Date: 2021-10-08 10:52:41 +0100 (Fri, 08 Oct 2021)
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

// Updates the Kubernetes GitOps repo (which ArgoCD watches) when new builds are created
//
// Requires the following environment variables to be already set in the pipeline environment{} section:
//
//    APP
//    ENVIRONMENT
//    GIT_USERNAME
//    GIT_EMAIL
//    GITOPS_REPO
//    DOCKER_IMAGE
//    GIT_COMMIT - provided automatically by Jenkins
//
// Should be wrapped in an sshagent block like this:
//
//    container('git-kustomize') {
//      sshagent (credentials: ['my-ssh-key'], ignoreMissing: false) { ... }
//
// Could be adapted to take these as parameters if multiple GitOps updates were done in a single pipeline, but more likely those should be separate pipelines

def call(dockerImages=["$DOCKER_IMAGE"], timeoutMinutes=4){
  if (!dockerImages){
    throw new IllegalArgumentException("first arg of gitOpsK8sUpdate (dockerImages) is null or empty, please define in the calling pipeline")
  }
  String gitOpsLock = "GitOps Kubernetes Image Update - App: '$APP', Environment: '" + "$ENVIRONMENT".capitalize() + "'"
  echo "Acquiring Lock: $gitOpsLock"
  lock(resource: gitOpsLock, inversePrecedence: true){
    timeout(time: timeoutMinutes, unit: 'MINUTES'){
      // workaround for https://issues.jenkins.io/browse/JENKINS-42582
      withEnv(["SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"]) {
        gitSetup()
        retry(2){
          sh (
            label: 'gitOpsK8sUpdate',
            script: """#!/bin/bash
              set -euxo pipefail

              git clone --branch "\$ENVIRONMENT" "\$GITOPS_REPO" repo

              cd "repo/\$APP/\$ENVIRONMENT"

              #kustomize edit set image "\$GCR_REGISTRY/\$GCR_PROJECT/\$APP:\$GIT_COMMIT"
              #kustomize edit set image "\$DOCKER_IMAGE:\$GIT_COMMIT"

              # needs to be double quoted for Groovy to generate these kustomize commands for all docker images in the first arg list
              ${ dockerImages.collect{ "kustomize edit set image $it:$GIT_COMMIT" }.join("\n") }

              git diff

              git add -A

              if ! git diff-index --quiet HEAD; then
                git commit -m "updated \$APP \$ENVIRONMENT app image version to build \$GIT_COMMIT"
              fi

              git push
            """
          )
        }
      }
    }
  }
}
