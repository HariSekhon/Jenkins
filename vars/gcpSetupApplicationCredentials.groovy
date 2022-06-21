//
//  Author: Hari Sekhon
//  Date: 2021-09-01 12:50:03 +0100 (Wed, 01 Sep 2021)
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
//       G C P   S e t u p   A p p l i c a t i o n   C r e d e n t i a l s
// ========================================================================== //

// Requires base64 encoded GCP_SERVICEACCOUNT_KEY environment variable to be set in environment{} section of Jenkinsfile, see top level Jenkinsfile template


def call(timeoutMinutes=1){
  retry(2){
    timeout(time: "$timeoutMinutes", unit: 'MINUTES') {
      String label = 'Generating GCP Application Credential Key'
      echo "$label"
      sh (
        label: "$label",
        script: '''#!/bin/bash
          set -euxo pipefail
          if [ -n "${GCP_SERVICEACCOUNT_KEY:-}" ]; then
            # XXX: pipeline must set GOOGLE_APPLICATION_CREDENTIALS to match this to pick these up
            base64 --decode <<< "$GCP_SERVICEACCOUNT_KEY" > "$HOME/.gcloud/application-credentials.json.$BUILD_TAG"
          fi
        '''
      )
    }
  }
}
