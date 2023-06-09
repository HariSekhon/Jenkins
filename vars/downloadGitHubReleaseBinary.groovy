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
//          D o w n l o a d   G i t H u b   R e l e a s e   B i n a r y
// ========================================================================== //

// Downloading it for each run trades inbound bandwidth (free) for not using RAM for bigger Jenkins pods causing more scale and billable Kubernetes
//
// Downloading only takes 2 seconds in testing
//
// The alternative is using the docker image which will be cached but hold RAM for the entire duration of the pipeline, which is very RAM inefficient:
//
//    https://github.com/HariSekhon/Kubernetes-configs/blob/master/jenkins/base/jenkins-agent-pod.yaml

def call (repo, binary, version='latest') {
  if ( ! repo ) {
    error("no github 'owner/repo' passed for first arg to downloadGitHubReleaseBinary()")
  }
  if ( ! binary ) {
    error('no binary passed for second arg to downloadGitHubReleaseBinary()')
  }
  //  ! version instanceof String   does not work and
  //    version !instanceof String  is only available in Groovy 3
  if (version instanceof String == false) {
    error "non-string version passed to downloadGitHubReleaseBinary() function"
  }
  if (version.contains("'")) {
    error "invalid version given to downloadGitHubReleaseBinary(): $version"
  }
  String label = "Download '$binary' on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 3, unit: 'MINUTES') {
      echo "$label"
      if (version == 'latest') {
        installBinary(url: "https://github.com/$repo/releases/latest/download/$binary-{os}-{arch}")
      } else {
        installBinary(url: "https://github.com/$repo/releases/download/$version/$binary-{os}-{arch}")
      }
    }
  }
}
