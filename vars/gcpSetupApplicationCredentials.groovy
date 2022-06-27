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

// Requires:
//
//   - environment {} section at top level of Jenkinsfile with:
//     - base64 encoded GCP_SERVICEACCOUNT_KEY environment variable or passed as a first argument
//     - GOOGLE_APPLICATION_CREDENTIALS environment variable set to a path to store the key - see top level Jenkinsfile template for a good example path

def call(credential="$GCP_SERVICEACCOUNT_KEY", timeoutMinutes=1){
  retry(2){
    timeout(time: "$timeoutMinutes", unit: 'MINUTES') {
      String label = 'Generating GCP Application Credential Key'
      echo "$label"
      sh (
        label: "$label",
        script: '''#!/usr/bin/env bash
          set -euxo pipefail
          if [ -n "${GOOGLE_APPLICATION_CREDENTIALS:-}" ]; then
            keyfile="$GOOGLE_APPLICATION_CREDENTIALS"
            mkdir -p -v "$(dirname "$keyfile")"
            echo "Writing Google Application Credentials key file to '$keyfile'"
            base64 --decode <<< "$GCP_SERVICEACCOUNT_KEY" > "$keyfile"
          else
            echo '$GOOGLE_APPLICATION_CREDENTIALS is not set'
            exit 1
          fi
        '''
      )
    }
  }
}
