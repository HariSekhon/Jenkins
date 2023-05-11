//
//  Author: Hari Sekhon
//  Date: 2022-06-21 10:46:51 +0100 (Tue, 21 Jun 2022)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/Jenkins
//
//  Liceese: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//               D o c k e r   L o g i n   t o   D o c k e r H u b
// ========================================================================== //

// See Also:
//
//    gcpAuthDocker.groovy
//    gcrAuthDocker.groovy
//    garAuthDocker.groovy

def call (user='', pass='', registry='') {
  user = user ?: env.DOCKERHUB_USER ?: error('dockerLogin: username not specified and DOCKERHUB_USER not set in the environment')
  password = password ?: env.DOCKERHUB_TOKEN ?: error('dockerLogin: password/token not specified and DOCKERHUB_TOKEN not set in the environment')
  echo "Docker Login: $user"
  echo "Docker Registry: ${registry ?: 'DockerHub'}"
  withEnv(["USER=$user", "PASS=$pass", "REGISTRY=$registry"]) {
    // Bourne compatible
    //sh '''
    //  set -eux
    //  docker login $REGISTRY -u "$USER" -p "$PASS"
    //'''
    // requires Bash
    sh '''#!/usr/bin/env bash
      set -eux
      docker login $REGISTRY -u "$USER" --password-stdin <<< "$PASS"
    '''
  }
}
