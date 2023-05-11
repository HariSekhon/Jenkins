//
//  Author: Hari Sekhon
//  Date: 2022-06-30 18:00:52 +0100 (Thu, 30 Jun 2022)
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
//                  Check Environment Variables for Jenkins CLI
// ========================================================================== //

def call () {
  sh (
    label: 'Check Environment Variables for Jenkins CLI',
    script: '''#!/usr/bin/env bash
      set -eux
      # doesn't work with _
      for var in JENKINS_URL JENKINS_USER_ID JENKINS_API_TOKEN; do
        echo "Checking $var"
        if [ -z "${!var:-}" ]; then
          echo "$var is not set"
          exit 1
        fi
      done
      echo 'All necessary Jenkins CLI environment variables set'
    '''
  )
}
