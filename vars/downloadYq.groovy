//
//  Author: Hari Sekhon
//  Date: 2023-05-11 05:24:55 +0100 (Thu, 11 May 2023)
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
//                             D o w n l o a d   Y q
// ========================================================================== //

// Downloading it for each run trades inbound bandwidth (free) for not using RAM for bigger Jenkins pods causing more scale and billable Kubernetes
//
// Downloading Yq only takes 2 seconds in testing
//
// The alternative is using the docker image which will be cached but hold RAM for the entire duration of the pipeline, which is very RAM inefficient:
//
//    https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml

// get release version from:
//
//    https://github.com/mikefarah/yq/releases
//

def call (version='latest') {
  downloadGitHubReleaseBinary('mikefarah/yq', 'yq', version)
  sh (
    label: "Yq Version",
    script: '''
      set -eu
      yq --version
    '''
  )
}
