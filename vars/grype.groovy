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

// Requires a Jenkins agent with Docker available locally ie. not a Kubernetes agent which usually won't have this

def call (targetList=[], fail=true, timeoutMinutes=10) {
  label 'Grype'
  if (targetList) {
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
  container('grype') {
    timeout(time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        for (target in targets) {
          withEnv (["TARGET=$target"]) {
            echo "Grype scanning image '$TARGET' - informational only to see all issues"
            sh ' grype "$TARGET" --fail-on high --scope all-layers '

            if (fail) {
              echo "Grypescanning image '$TARGET' for HIGH/CRITICAL vulnerabilities - will fail if any are detected"
              sh ' grype "$TARGET" --fail-on high --scope all-layers '
            }
          }
        }
      }
    }
  }
}
