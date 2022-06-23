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

// ========================================================================== //
//                     G i t   K u s t o m i z e   I m a g e
// ========================================================================== //

// Updates the Kubernetes GitOps repo (which ArgoCD watches) when new builds are created
//
//  XXX: the first argument should be a list of Docker Images including any repo prefix, eg. 'eu.gcr.io/$CLOUDSDK_CORE_PROJECT/myapp' for Kustomize to be able to update the right image reference in the K8s yaml
//
//
// Requires the following environment variables to be already set in the pipeline environment{} section:
//
//    GITOPS_REPO
//    GIT_USERNAME
//    GIT_EMAIL
//
//    GIT_COMMIT - provided automatically by Jenkins
//
//  If dockerImages list isn't passed as first argument:
//
//    DOCKER_IMAGE
//
//  Only if dir is not passed, infers by convention <$APP>/<$ENVIRONMENT>/ as the directory path in the repo to update
//
//    APP
//    ENVIRONMENT
//
// Should be wrapped in an sshagent block like this:
//
//    container('git-kustomize') {
//      sshagent (credentials: ['my-ssh-key'], ignoreMissing: false) { ... }
//
// Could be adapted to take these as parameters if multiple GitOps updates were done in a single pipeline, but more likely those should be separate pipelines

def call(dockerImages=["$DOCKER_IMAGE"], dir, version="$GIT_COMMIT", timeoutMinutes=5){
  if (!dockerImages) {
    throw new IllegalArgumentException("first arg of gitKustomizeImage (dockerImages) is null or empty, please define in the calling pipeline")
  }
  assert dockerImages instanceof Collection
  if (!dir) {
    throw new IllegalArgumentException("second arg of gitKustomizeImage (dir) is null or empty, please define in the calling pipeline")
  }
  String label = "Git Kustomize Image Version - Dir: '$dir'"
  echo "Acquiring gitKustomizeImage Lock: $label"
  lock(resource: label, inversePrecedence: true){
    milestone ordinal: null, label: "Milestone: $label"
    timeout(time: timeoutMinutes, unit: 'MINUTES'){
      // workaround for https://issues.jenkins.io/browse/JENKINS-42582
      withEnv(["SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"]) {
        gitSetup()
        retry(2){
          sh (
            label: "$label",
            // needs to be double quoted for Groovy to generate these kustomize commands for all docker images in the first arg list
            script: """#!/bin/bash
              set -euxo pipefail

              # copy local repo's user and email setting from this pipeline to the cloned repo
              GIT_USERNAME="\$(git config user.name)"
              GIT_EMAIL="\$(git config user.email)"

              git clone --branch "\$ENVIRONMENT" "\$GITOPS_REPO" repo

              cd "repo/$dir"

              git config user.name "\$GIT_USERNAME"
              git config user.email "\$GIT_EMAIL"

              #kustomize edit set image "\$GCR_REGISTRY/\$GCR_PROJECT/\$APP:\$version"
              #kustomize edit set image "\$DOCKER_IMAGE:\$version"

              ${ dockerImages.collect{ "kustomize edit set image $it:$version" }.join("\n") }

              git diff

              git add -A

              if ! git diff-index --quiet HEAD; then
                git commit -m "updated app images under '$dir' to version '\$version'"
              fi

              git push
            """
          )
        }
      }
    }
  }
}
