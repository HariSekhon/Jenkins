//
//  Author: Hari Sekhon
//  Date: 2023-05-15 05:38:38 +0100 (Mon, 15 May 2023)
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
//                                   C l a i r
// ========================================================================== //

// https://github.com/quay/clair

// Clair container security scanner

// Pass list of Docker image:tag images (will attempt to infer from environment DOCKER_IMAGE and DOCKER_TAG otherwise)
//
// Set CLAIR_URL, otherwise defaults to http://clair.clair.svc.cluster.local:8080

// Requires:
//
// - a Jenkins agent with Docker available locally see https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml
//
// - if pulling docker images from Google Container Registry or Google Artifact Registry then be sure to set up Google Application Credentials first by calling
//   gcpSetupApplicationCredentials.groovy or setting up general Docker Authentication to GCR/GAR by calling gcpDockerAuth.groovy / gcrDockerAuth.groovy / garDockerAuth.groovy
//
// Usage:
//
//      clair("docker_image1:tag")  // pass a string for a single image
//
//      clair(["docker_image1:tag1", "docker_image2:tag2"])  // pass a list for 2 of more images
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        clair(["docker_image1:tag1", "docker_image2:tag2"])
//              // or
//        clair(env.DOCKER_IMAGES_TAGS.split(',') as List)
//      }
//

// If wanting to call without args, must specify a default type otherwise will hit this error:
//
//    org.codehaus.groovy.runtime.metaclass.MethodSelectionException: Could not find which method call() to invoke from this list:
//
def call (imageList=[], timeoutMinutes=30) {
  label 'Clair'
  List images = []
  if (imageList) {
    images = imageList
  } else {
    images = dockerInferImageTagList()
  }
  env.CLAIR_URL = env.CLAIR_URL ?: 'http://clair.clair.svc.cluster.local:8080'
  // let caller decide if wrapping this in a container('clairctl') or using downloadClairctl.groovy to save RAM
  //container('clairctl') {
    timeout (time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        for (image in images) {
          withEnv (["image=$image"]) {
            echo "Clair scanning container image '$image'"
            sh (
              label: "Clair",
              script: "clairctl -D report --host '$CLAIR_URL' '$image'"
            )
          }
        }
      }
    }
  //}
}
