//
//  Author: Hari Sekhon
//  Date: 2021-09-01 12:50:03 +0100 (Wed, 01 Sep 2021)
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
//                          G C P   C l o u d B u i l d
// ========================================================================== //

// must run gcpActiveServiceAccount() first to authenticate
//
// Example call:
//
//    // single quote to evaluate the variables in the shell so we can use Bash to strip off the origin/ prefix for the Git Branch
//    gcpCloudBuild(args: '--project="$GCR_PROJECT" --substitutions="_REGISTRY=$GCR_REGISTRY,_IMAGE_VERSION=$GIT_COMMIT,_GIT_BRANCH=${GIT_BRANCH##*/}"', timeoutMinutes: 30)
//
//  If dockerImages list argument is set, will check for each of those docker 'image:tag' exist in GCR and skip running CloudBuild if all of them are already present
//  - this can save a lot of time in CI/CD deployment re-runs and is an optimization worth setting:
//
//    // double quote to interpolate the variables in Groovy to check $GCR_REGISTRY and $GCR_PROJECT were actually set in the pipeline
//       - since this happens before CloudBuild is run with the args, catches errors earlier for you
//    gcpCloudBuild(dockerImages: ["$GCR_REGISTRY/$GCR_PROJECT/myapp:$GIT_COMMIT",
//                                 "$GCR_REGISTRY/$GCR_PROJECT/myapp2:$GIT_COMMIT"])

def call (Map args = [args:'', skipIfdockerImagesExist: [], timeoutMinutes:60]) {
  // XXX: prevents calling in a parallel stage otherwise you'll get this error:
  //
  //  "Using a milestone step inside parallel is not allowed"
  //
  milestone ordinal: null, label: "Milestone: Build"
  echo "Building from branch '$GIT_BRANCH'"
  // set defaults if these args aren't passed
  args.args = args.args ?: ''
  args.timeoutMinutes = args.timeoutMinutes ?: 60
  args.skipIfDockerImagesExist = args.skipIfDockerImagesExist ?: []
  if (! args.skipIfDockerImagesExist && env.DOCKER_IMAGES) {
    args.skipIfDockerImagesExist = dockerInferImageTagList()
  }
  retry (2) {
    timeout (time: "${args.timeoutMinutes}", unit: 'MINUTES') {
      script {
        boolean dockerImagesExist = false
        if (args.skipIfDockerImagesExist) {
          List dockerImageTags = []
          // if we're passed a string just convert it to a list for convenience
          dockerImageTags = stringToList(args.skipIfDockerImagesExist)
          String labelCheckingImages = 'Checking if Docker images exist in GCR'
          echo "$labelCheckingImages"
          dockerImagesExist =
            sh (
              label: "$labelCheckingImages",
              returnStatus: true,
              script: """#!/usr/bin/env bash
                set -euxo pipefail

                export CLOUDSDK_CORE_DISABLE_PROMPTS=1

                gcloud auth list

                for docker_image_tag in ${ dockerImageTags.join(" ") }; do
                  if [[ "\$docker_image_tag" =~ : ]]; then
                    docker_image="\${docker_image_tag%%:*}"
                    docker_tag="\${docker_image_tag##*:}"
                    if ! gcloud container images list-tags "\$docker_image" --filter="tags:\$docker_tag" --format=text | grep -q .; then
                      exit 1
                    fi
                  else
                    exit 1
                  fi
                done
              """
            ) == 0  // convert the exit code to a boolean
        }
        if ( ! dockerImagesExist ) {
          String label = 'Running GCP CloudBuild'
          echo "$label"
          sh (
            label: "$label",
            script: """#!/usr/bin/env bash
              set -euxo pipefail

              export CLOUDSDK_CORE_DISABLE_PROMPTS=1

              gcloud auth list

              gcloud builds submit --timeout "${args.timeoutMinutes}m" ${args.args}
            """
          )
        }
      }
    }
  }
}
