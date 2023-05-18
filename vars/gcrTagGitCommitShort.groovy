//
//  Author: Hari Sekhon
//  Date: 2023-03-09 16:07:58 +0000 (Thu, 09 Mar 2023)
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
//                G C R   T a g   G i t   S h o r t   C o m m i t
// ========================================================================== //

// Tags the current GIT_COMMIT tagged Docker image in Google Container Registry with the Git Short Commit which is a bit more user friendly when using not explicitly versioned CI/CD deployments
//
// Call with a string split like for an env var:
//
//    gcrTagGitCommitShort(env.DOCKER_IMAGES.split(',') as List)
//
// Requires GCloud SDK CLI to be installed and authenticated

def call (List<String> dockerImageRegistryPaths=[]) {
  if ( ! env.GIT_COMMIT_SHORT ) {
    gitCommitShort()
  }
  dockerImageRegistryPaths = dockerImageRegistryPaths ?: dockerInferImageList()
  if (dockerImageRegistryPaths instanceof List == false) {
    error "non-list passed as arg to gcrTagGitCommitShort()"
  }
  for (String dockerImageRegistryPath in dockerImageRegistryPaths) {
    if (gcrDockerImageExists(dockerImageRegistryPath, env.GIT_COMMIT_SHORT)) {
      continue
    }
    if (!gcrDockerImageExists(dockerImageRegistryPath, env.GIT_COMMIT)) {
      error "Docker image '$dockerImageRegistryPath' full SHA '$GIT_COMMIT' tag does not exist, cannot tag with short SHA!"
    }
    gcrTagDockerImage("$dockerImageRegistryPath:$GIT_COMMIT", env.GIT_COMMIT_SHORT)
  }
}
