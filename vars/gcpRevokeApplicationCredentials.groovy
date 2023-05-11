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
//      G C P   R e v o k e   A p p l i c a t i o n   C r e d e n t i a l s
// ========================================================================== //

// Removes the application default credentials file
//
// Use this if you're using long-running agents
//
// Not needed on Kubernetes agents which are ephemeral

def call (timeoutMinutes=1) {
  retry (2) {
    timeout (time: "$timeoutMinutes", unit: 'MINUTES') {
      String label = 'Deleting GCP Application Credential Key'
      echo "$label"
      sh (
        label: "$label",
        script: '''#!/usr/bin/env bash
          set -euxo pipefail
          # XXX: must match gcpSetupApplicationCredentials.groovy
          keyfile="$WORKSPACE_TMP/.gcloud/application-credentials.json.$BUILD_TAG"
          rm -fv "$keyfile"
        '''
      )
    }
  }
}
