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

// Pass the target as the first arg, otherwise requires DOCKER_IMAGE and DOCKER_TAG to be set in environment{} section of Jenkinsfile pipeline

def call(target="$DOCKER_IMAGE:$DOCKER_TAG", timeoutMinutes=10){
  label 'Trivy'
  container('trivy') {
    timeout(time: timeoutMinuntes, unit: 'MINUTES') {
      ansiColor('xterm') {
        withEnv(["TARGET=$target"]){
          // informational to see all issues
          sh ' trivy image --no-progress "$TARGET" '

          // fail the pipeline if any of the issues are High / Critical
          sh ' trivy image --no-progress --exit-code 1 --severity HIGH,CRITICAL "$TARGET" '
        }
      }
    }
  }
}
