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

// Security scanner

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
//      trivy(targets: ["docker_image1:tag1", "docker_image2:tag2"], fail: true, timeoutMinutes: 15)
//
// XXX: doesn't work at this time due to issue:
//
//    https://github.com/anchore/grype/issues/1287

def call (Map args = [targets=[], fail=true, timeoutMinutes=10]) {
  label 'Grype'
  fail = args.fail == false ? false : true
  timeoutMinutes = args.timeoutMinutes ?: 10
  if (args.targets) {
    targets = args.targets
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
  container('grype') {
    timeout(time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        for (target in targets) {
          withEnv (["TARGET=$target"]) {
            echo "Grype scanning image '$TARGET' - informational only to see all issues"
            sh (
              label: "Grype",
              script: ' grype "$TARGET" --fail-on high --scope all-layers '
            )

            if (fail) {
              echo "Grype scanning image '$TARGET' for HIGH/CRITICAL vulnerabilities - will fail if any are detected"
              sh (
                label: "Grype",
                script: ' grype "$TARGET" --fail-on high --scope all-layers '
              )
            }
          }
        }
      }
    }
  }
}
