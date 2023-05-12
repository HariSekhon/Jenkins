//
//  Author: Hari Sekhon
//  Date: 2022-01-06 17:19:11 +0000 (Thu, 06 Jan 2022)
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
//                T r i v y   S c a n   D o c k e r   I m a g e s
// ========================================================================== //

// Security scanner

// Pass list of Docker image:tag targets (will attempt to infer from environment DOCKER_IMAGE and DOCKER_TAG otherwise)

// For requirements see adjacent trivy.groovy
//
// Usage:
//
//      trivyScanDockerImages("docker_image1:tag")  // pass a string for a single image
//
//      trivyScanDockerImages(["docker_image1:tag1", "docker_image2:tag2"])  // pass a list for 2 or more images
//
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        trivyScanDockerImages(["docker_image1:tag1", "docker_image2:tag2"])
//              // or
//        trivyScanDockerImages(env.DOCKER_IMAGES.split(',') as List)
//      }
//

def call (imageList=[], fail=true, timeoutMinutes=30) {
  label 'Trivy'
  if (imageList) {
    images = imageList
    if (imageList instanceof String) {
      images = [imageList]
    }
    //  ! targetList instanceof List   does not work and
    //    targetList !instanceof List  is only available in Groovy 3
    if (images instanceof List == false) {
      error "non-list passed as first arg to trivyScanDockerImages() function"
    }
  } else {
    if (env.DOCKER_IMAGE) {
      String tag = 'latest'
      if (env.DOCKER_TAG) {
        tag = env.DOCKER_TAG
      }
      images = ["$DOCKER_IMAGE:$tag"]
    } else {
      error "No docker images passed to trivyScanDockerImages() function and no \$DOCKER_IMAGE / \$DOCKER_TAG environment variable found"
    }
  }
  timeout (time: timeoutMinutes, unit: 'MINUTES') {
    for (image in images) {
      withEnv (["IMAGE=$image"]) {
        echo "Trivy scanning image '$IMAGE' - informational only to see all issues"
        trivy("image --no-progress --timeout ${timeoutMinutes}m $IMAGE")
        if (fail) {
          echo "Trivy scanning image '$IMAGE' for HIGH/CRITICAL vulnerabilities - will fail if any are detected"
          trivy("image --no-progress --timeout ${timeoutMinutes}m --exit-code 1 --severity HIGH,CRITICAL $IMAGE")
        }
      }
    }
  }
}
