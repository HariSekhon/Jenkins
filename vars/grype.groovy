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
//                                   G r y p e
// ========================================================================== //

// https://github.com/anchore/grype

// Grype security scanner

// Pass list of Docker image:tag targets (will attempt to infer from environment DOCKER_IMAGE and DOCKER_TAG otherwise)

// Requires:
//
// - a Jenkins agent with Docker available locally see https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml
//
// - if pulling docker images from Google Container Registry or Google Artifact Registry then be sure to set up Google Application Credentials first by calling
//   gcpSetupApplicationCredentials.groovy or setting up general Docker Authentication to GCR/GAR by calling gcpDockerAuth.groovy / gcrDockerAuth.groovy / garDockerAuth.groovy
//
// Usage:
//
//      grype("docker_image1:tag")  // pass a string for a single image
//
//      grype(["docker_image1:tag1", "docker_image2:tag2"])  // pass a list for 2 of more images
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        grype(["docker_image1:tag1", "docker_image2:tag2"])
//              // or
//        grype(env.DOCKER_IMAGES_TAGS.split(',') as List)
//      }
//

// If wanting to call without args, must specify a default type otherwise will hit this error:
//
//    org.codehaus.groovy.runtime.metaclass.MethodSelectionException: Could not find which method call() to invoke from this list:
//
def call (targetList=[], failOn='high', timeoutMinutes=30) {
  label 'Grype'
  List targets = []
  if (targetList) {
    targets = targetList
  } else {
    targets = dockerInferImageTagList()
  }
  // let caller decide if wrapping this in a container('grype') or using downloadGrype.groovy to save RAM
  //container('grype') {
    timeout (time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        for (target in targets) {
          withEnv (["TARGET=$target", "FAIL_ON=$failOn"]) {
            echo "Grype scanning target '$TARGET'"
            sh (
              label: "Grype",
              // still shows medium when --fail-on high
              script: "grype '$TARGET' --verbose --scope all-layers --fail-on '$FAIL_ON'"
            )
          }
        }
      }
    }
  //}
}
