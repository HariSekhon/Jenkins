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
// Optional environment variables:
//
//    GIT_USERNAME
//    GIT_EMAIL
//
// Provided automatically by Jenkins:
//
//    GIT_COMMIT
//
// Should be wrapped in an sshagent block like this:
//
//    container('git-kustomize') {
//      sshagent (credentials: ['my-ssh-key'], ignoreMissing: false) { ... }
//
// Could be adapted to take these as parameters if multiple GitOps updates were done in a single pipeline, but more likely those should be separate pipelines

def call (Map args = [:]) {

  // these get blocked in Jenkins Groovy Sandbox
  //if (!args.dockerImages) {
  //  throw new IllegalArgumentException("dockerImages not provided to gitKustomizeImage() function")
  //}
  //if (!args.dir) {
  //  throw new IllegalArgumentException("dir not provided to gitKustomizeImage() function")
  //}
  //if (!args.repo) {
  //  throw new IllegalArgumentException("repo arg not provided to gitKustomizeImage() function")
  //}
  //assert args.dockerImages instanceof Collection
  //assert args.dir instanceof String
  //assert args.repo instanceof String

  args.dockerImages = args.dockerImages ?: dockerInferImageList()
  args.repo = args.repo ?: env.GITOPS_REPO ?: error("no repo arg passed to gitKustomizeImage() and \$GITOPS_REPO environment variable is not set")
  args.branch  = args.branch ?: env.GITOPS_BRANCH ?: 'main'
  args.dir = args.dir ?: env.K8S_DIR ?: '.'
  args.timeoutMinutes = args.timeout ?: 5
  args.version = args.version ?:
                 env.GIT_COMMIT_SHORT ?:
                 env.GIT_COMMIT ?:
                 error("no version arg passed to gitKustomizeImage() and \$GIT_COMMIT environment variable is not set")

  if (args.dockerImages instanceof List == false) {
    error "non-list passed as dockerImages arg of gitKustomizeImage()"
  }

  // Lock should be on the Owner/Repo format, not including the protocol prefix because some pipelines may use git@github.com while others use https://github.com
  //
  // git@github.com:HariSekhon/Kubernetes -> HariSekhon/Kubernetes
  String ownerRepo = args.repo.split(':')[-1]
  //
  // https://github.com/HariSekhon/Kubernetes -> HariSekhon/Kubernetes
  ownerRepo = ownerRepo.split('/')[-2, -1].join('/')
  //
  // HariSekhon/Kubernetes.git -> HariSekhon/Kubernetes
  ownerRepo = ownerRepo.split('\\.')[0]
  //
  // normalize to lowercase so different users don't bypass the lock via capitalization. Branch and Dir are case sensitive, so those would fail naturally and don't need normalizing
  ownerRepo = ownerRepo.toLowerCase()

  String label = "Git Kustomize Image Version - Repo: '$ownerRepo', Branch: '$args.branch', Dir: '$args.dir'"
  echo "Acquiring gitKustomizeImage Lock: $label"
  lock (resource: label, inversePrecedence: true) {
    // XXX: prevents calling in a parallel stage otherwise you'll get this error:
    //
    //  "Using a milestone step inside parallel is not allowed"
    //
    milestone ordinal: null, label: "Milestone: $label"
    timeout (time: args.timeoutMinutes, unit: 'MINUTES') {
      // workaround for https://issues.jenkins.io/browse/JENKINS-42582
      withEnv(["SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"]) {
        gitSetup()
        retry (2) {
          sh (
            label: "$label",
            // needs to be double quoted for Groovy to generate these kustomize commands for all docker images in the first arg list
            script: """
              set -eux

              # copy local repo's user and email setting from this pipeline to the cloned repo
              # if this wasn't set then it'll be set up to sane defaults by gitSetup() function called above
              GIT_USERNAME="\$(git config user.name)"
              GIT_EMAIL="\$(git config user.email)"

              repo_dir="${args.repo}"
              repo_dir="\${repo_dir##*/}"

              if ! [ -d "\$repo_dir" ]; then
                git clone --branch "${args.branch}" "${args.repo}" "\$repo_dir"
              fi

              cd "\$repo_dir/${args.dir}"

              git checkout "${args.branch}" --force

              git pull

              git config user.name "\$GIT_USERNAME"
              git config user.email "\$GIT_EMAIL"

              #kustomize edit set image "\$GCR_REGISTRY/\$GCR_PROJECT/\$APP:\$version"
              #kustomize edit set image "\$DOCKER_IMAGE:\$version"

              ${ args.dockerImages.collect { "kustomize edit set image $it:${args.version}" }.join("\n") }

              git diff

              git add -A

              git status

              if ! git diff-index --quiet HEAD; then
                git commit -m "updated app images under '${args.dir}' to version '${args.version}'"

                # XXX: push is done here and not a separate stage (which would be nicer visually in a Blue Ocean pipeline)
                #      because we need the lock to encompass the entire operation for safety
                git push
              fi
            """
          )
        }
      }
    }
  }
}
