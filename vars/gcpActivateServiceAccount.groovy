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

// Requires base64 encoded GCP_SERVICEACCOUNT_KEY environment variable to be set in environment{} section of Jenkinsfile, see top level Jenkinsfile template

// TODO: if you're not calling this on dynamic short-lived K8s agents, you'll need to add a unique cache for each build to prevent race conditions if using different credentials between pipelines
def call(timeoutMinutes=2){
  retry(2){
    timeout(time: "$timeoutMinutes", unit: 'MINUTES') {
      String label = 'Activating GCP Service Account credential'
      echo "$label"
      sh (
        label: "$label",
        script: '''#!/bin/bash
          set -euxo pipefail
          base64 --decode <<< "$GCP_SERVICEACCOUNT_KEY" > credentials.json
          gcloud auth activate-service-account --key-file=credentials.json
          rm -f credentials.json
          gcloud auth list
        '''
      )
    }
  }
}
