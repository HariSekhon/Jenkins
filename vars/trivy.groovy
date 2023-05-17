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
//                                   T r i v y
// ========================================================================== //

// Trivy security scanner

// Usage:
//
//  If you're running on a Jenkins agent that already has the trivy binary bundled just call it otherwise download trivy first
//  Downloading Trivy only takes 7 seconds in testing
//
//      downloadTrivy()
//
//      trivy('...')
//
//
//  If you've set up a Trivy container in your Jenkins agent pod like: https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml
//
//      container('trivy') {
//        trivy('...')
//      }
//
//  If you want to make it informational but not break the build:
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        trivy('...')
//      }
//
// Requires:
//
// - a Jenkins agent with Docker available locally see https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml
//
// - if pulling docker images from Google Container Registry or Google Artifact Registry then be sure to set up Google Application Credentials first by calling
//   gcpSetupApplicationCredentials.groovy or setting up general Docker Authentication to GCR/GAR by calling gcpDockerAuth.groovy / gcrDockerAuth.groovy / garDockerAuth.groovy
//
// - if using trivy container and using Google Application Credentials then make sure to set them up in the container to be used later eg.
//
//      withCredentials([string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')]) {
//        container('trivy') {
//          gcpSetupApplicationCredentials()
//        }
//      }
//
// XXX: set environment variable TRIVY_SERVER to use a Trivy server to not waste 15 minutes downloading the vulnerabilities DB on every Jenkins agent,
//      especially important if you're using auto-spawning agents on Kubernetes. On Kubernetes this should be set in Jenkins set this globally at $JENKINS_URL/configure to:
//
//        TRIVY_SERVER=http://trivy.trivy.svc.cluster.local:4954
//
//        https://github.com/HariSekhon/Kubernetes-configs/tree/master/trivy/base
//
// XXX: don't forget to set TRIVY_DEBUG=true for better logging
//

def call (args='') {
  label 'Trivy'
  // let caller decide if wrapping this in a container('trivy') or using downloadTrivy.groovy to save RAM
  //container('trivy') {
    ansiColor('xterm') {
      sh (
        label: "Trivy",
        script: "trivy $args"
      )
    }
  //}
}
