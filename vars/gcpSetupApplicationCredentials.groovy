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
//     - base64 encoded GCP_SERVICEACCOUNT_KEY environment variable
//        or wrapped in something like
//     - withCredentials([string(credentialsId: 'gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')]) {
//     - GOOGLE_APPLICATION_CREDENTIALS environment variable set to a path to store the key - see top level Jenkinsfile template for a good example path

def call (timeoutMinutes=1) {
  retry (2) {
    timeout (time: "$timeoutMinutes", unit: 'MINUTES') {
      String label = 'Generating GCP Application Credential Key'
      echo "$label"
      sh (
        label: "$label",
        // needs to be bash to use <<< to avoid exposing the GCP_SERVICEACCOUNT_KEY in shell tracing
        //script: '''#!/bin/sh
        // but many docker containers like Trivy don't have Bash :'-(
        //  # XXX: don't set -x as it'll expose the Service Account key credential
        //  # base64 --decode is portable across Linux and Mac, but unfortunately busybox as found in Trivy container only supports -d
        //  #base64 --decode <<< "$GCP_SERVICEACCOUNT_KEY" > "$keyfile"
        script: '''#!/bin/sh
          set -eu
          if [ -z "${GOOGLE_APPLICATION_CREDENTIALS:-}" ]; then
            echo '$GOOGLE_APPLICATION_CREDENTIALS is not set'
            exit 1
          fi
          if [ -z "${GCP_SERVICEACCOUNT_KEY:-}" ]; then
            echo '$GCP_SERVICEACCOUNT_KEY is not set'
            exit 1
          fi

          keyfile="$GOOGLE_APPLICATION_CREDENTIALS"

          mkdir -p -v "$(dirname "$keyfile")"

          echo "Writing Google Application Credentials key file to '$keyfile'"

          echo "$GCP_SERVICEACCOUNT_KEY"| base64 -d > "$keyfile"
        '''
      )
    }
  }
}
