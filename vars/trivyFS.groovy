//
//  Author: Hari Sekhon
//  Date: 2023-05-12 17:45:24 +0100 (Fri, 12 May 2023)
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
//      trivyFs()  // scan the git clone checkout in the root directory where we start the pipeline
//
//      trivyFs('src') // scan only the src code directory
//
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        trivyFs()
//      }
//
// XXX: set environment variable TRIVY_SERVER to use a Trivy server to not waste 15 minutes downloading the vulnerabilities DB on every Jenkins agent,
//      especially important if you're using auto-spawning agents on Kubernetes. On Kubernetes this should be set in Jenkins set this globally at $JENKINS_URL/configure to:
//
//        TRIVY_SERVER=http://trivy.trivy.svc.cluster.local:4954
//
//        https://github.com/HariSekhon/Kubernetes-configs/tree/master/trivy/base
//

def call (dir='.', fail=true, timeoutMinutes=10) {
  label 'Trivy'
  timeout (time: timeoutMinutes, unit: 'MINUTES') {
    withEnv (["DIR=$dir"]) {
      echo "Trivy scanning dir '$DIR' - informational only to see all issues"
      trivy("fs '$DIR' --no-progress --timeout ${timeoutMinutes}m")
      if (fail) {
        echo "Trivy scanning dir '$DIR' for HIGH/CRITICAL vulnerabilities - will fail if any are detected"
        trivy("fs '$DIR' --no-progress --timeout ${timeoutMinutes}m --exit-code 1 --severity HIGH,CRITICAL")
      }
    }
  }
}
