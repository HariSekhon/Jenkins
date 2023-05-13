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
//             T r i v y   S c a n   C o n t a i n e r   I m a g e s
// ========================================================================== //

// Security scanner

// Pass list of Docker image:tag targets (will attempt to infer from environment DOCKER_IMAGE and DOCKER_TAG otherwise)

// For requirements see adjacent trivy.groovy
//
// Usage:
//
//      trivyImages("docker_image1:tag")  // pass a string for a single image
//
//      trivyImages(["docker_image1:tag1", "docker_image2:tag2"])  // pass a list for 2 or more images
//
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        trivyImages(["docker_image1:tag1", "docker_image2:tag2"])
//              // or
//        trivyImages(env.DOCKER_IMAGES_TAGS.split(',') as List)
//      }
//
// XXX: set environment variable TRIVY_SERVER to use a Trivy server to not waste 15 minutes downloading the vulnerabilities DB on every Jenkins agent,
//      especially important if you're using auto-spawning agents on Kubernetes. On Kubernetes this should be set in Jenkins set this globally at $JENKINS_URL/configure to:
//
//        TRIVY_SERVER=http://trivy.trivy.svc.cluster.local:4954
//
//        https://github.com/HariSekhon/Kubernetes-configs/tree/master/trivy/base
//

def call (imageList=[], severity='HIGH,CRITICAL', timeoutMinutes=30) {
  label 'Trivy'
  if (imageList) {
    images = imageList
    if (imageList instanceof String ||
        imageList instanceof org.codehaus.groovy.runtime.GStringImpl) {
      images = [imageList]
    }
    //  ! targetList instanceof List   does not work and
    //    targetList !instanceof List  is only available in Groovy 3
    if (images instanceof List == false) {
      error "non-list passed as first arg to trivyImages() function"
    }
  } else {
    if (env.DOCKER_IMAGE) {
      String tag = 'latest'
      if (env.DOCKER_TAG) {
        tag = env.DOCKER_TAG
      }
      images = ["$DOCKER_IMAGE:$tag"]
    } else {
      error "No docker images passed to trivyImages() function and no \$DOCKER_IMAGE / \$DOCKER_TAG environment variable found"
    }
  }
  timeout (time: timeoutMinutes, unit: 'MINUTES') {
    for (image in images) {
      withEnv (["IMAGE=$image", "SEVERITY=$severity"]) {
        // Trivy won't show anything below --severity so need to run one without severity to get the full information
        echo "Trivy scanning image '$IMAGE' - informational only to see all issues"
        trivy("image --no-progress --timeout '${timeoutMinutes}m' '$IMAGE'")

        String filename = 'trivy-image-' + image.split(':')[0].replace('/', '_') + '.html'
        echo "Trivy generate report for image '$IMAGE'"
        sh('mkdir -p reports')
        trivy("image --no-progress --timeout '${timeoutMinutes}m' '$IMAGE' --format template --template '@trivy-html.tpl' -o 'reports/$filename'")
        echo "Publish HTML report to Jenkins for image '$IMAGE'"
        publishHTML target : [
          allowMissing: true,
          alwaysLinkToLastBuild: true,
          keepAll: true,
          reportDir: 'reports',
          reportFiles: "$filename",
          reportName: "Trivy Image Scan '$IMAGE'",
          reportTitles: "Trivy Image Scan '$IMAGE'"
        ]

        echo "Trivy scanning image '$IMAGE' for severity '$SEVERITY' vulnerabilities only - will fail if any are detected"
        trivy("image --no-progress --timeout '${timeoutMinutes}m' --exit-code 1 --severity '$SEVERITY' '$IMAGE'")
      }
    }
  }
}
