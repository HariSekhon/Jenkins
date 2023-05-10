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

// Pass list of Docker image:tag targets (will attempt to infer from environment DOCKER_IMAGE and DOCKER_TAG otherwise

def call(targets=[], fail=true, timeoutMinutes=10){
  label 'Trivy'
  if(!targets){
    if(env.DOCKER_IMAGE){
      String tag = 'latest'
      if(env.DOCKER_TAG){
        tag = env.DOCKER_TAG
      }
      targets = ["$DOCKER_IMAGE:$tag"]
    } else {
      error "No targets passed to trivy() function and no \$DOCKER_IMAGE / \$DOCKER_TAG environment variable found"
    }
  }
  container('trivy') {
    timeout(time: timeoutMinuntes, unit: 'MINUTES') {
      ansiColor('xterm') {
        for(target in targets){
          withEnv(["TARGET=$target"]){
            echo "Trivy scanning image '$TARGET' - informational only to see all issues"
            sh ' trivy image --no-progress "$TARGET" '

            if(fail){
              echo "Trivy scanning image '$TARGET' for HIGH/CRITICAL vulnerabilities - will fail if any are detected"
              sh ' trivy image --no-progress --exit-code 1 --severity HIGH,CRITICAL "$TARGET" '
            }
          }
        }
      }
    }
  }
}
