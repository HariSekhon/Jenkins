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
// - if pulling from Google Container Registry - gcpSetupApplicationCredentials.groovy (adjacent) run in the trivy container before calling this function eg.
//
//      withCredentials([string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')]) {
//        container('trivy'){
//          gcpSetupApplicationCredentials()
//        }
//      }
//
//
// Usage:
//
//      trivy(["docker_image1:tag1", "docker_image2:tag2"])
//
//  You may want to wrap this in a catchError to show the stage failed but continue to the Deployment as its quite common for there to be some alerts for this and you don't want it blocking people
//
//      catchError(stageResult: 'FAILURE') {
//        grype(["docker_image1:tag1", "docker_image2:tag2"])
//        // or
//        grype(env.DOCKER_IMAGES.split(',') as List)
//      }
//

def call (targetList=[], fail=true, timeoutMinutes=10) {
  label 'Grype'
  if (targetList) {
    if (targetList instanceof String) {
      targetList = [targetList]
    }
    //  ! targetList instanceof List   does not work and
    //    targetList !instanceof List  is only available in Groovy 3
    if (targetList instanceof List == false) {
      error "non-list passed as first arg to grype() function"
    }
    targets = targetList
  } else {
    if (env.DOCKER_IMAGE) {
      String tag = 'latest'
      if (env.DOCKER_TAG) {
        tag = env.DOCKER_TAG
      }
      targets = ["$DOCKER_IMAGE:$tag"]
    } else {
      error "No targets passed to grype() function and no \$DOCKER_IMAGE / \$DOCKER_TAG environment variable found"
    }
  }
  // let caller decide if wrapping this in a container('grype') or using downloadGrype.groovy to save RAM
  //container('grype') {
    timeout(time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        for (target in targets) {
          withEnv (["TARGET=$target"]) {
            echo "Grype scanning target '$TARGET' - informational only to see all issues"
            sh (
              label: "Grype",
              script: "grype '$TARGET' --scope all-layers"
            )

            if (fail) {
              echo "Grype scanning target '$TARGET' for HIGH/CRITICAL vulnerabilities - will fail if any are detected"
              sh (
                label: "Grype",
                script: "grype '$TARGET' --fail-on high --scope all-layers"
              )
            }
          }
        }
      }
    }
  //}
}
