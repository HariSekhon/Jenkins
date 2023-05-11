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

// Security scanner

// For Usage see adjacent trivyScanDockerImages.groovy
//
// Requires:
//
// - a Jenkins agent with Docker available locally see https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml
//
// - if using trivy container and pulling from Google Container Registry - gcpSetupApplicationCredentials.groovy (adjacent)
//   then set up the GCP application credentials key file in the trivy container before calling this function eg.
//
//      withCredentials([string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')]) {
//        container('trivy'){
//          gcpSetupApplicationCredentials()
//        }
//      }
//

def call (args='') {
  label 'Trivy'
  // let caller decide if wrapping this in a trivy container or using downloadTrivy to save RAM
  //container('trivy') {
    ansiColor('xterm') {
      sh (
        label: "Trivy",
        script: "trivy $args"
      )
    }
  //}
}
