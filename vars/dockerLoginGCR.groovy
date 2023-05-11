//
//  Author: Hari Sekhon
//  Date: 2022-06-21 10:55:43 +0100 (Tue, 21 Jun 2022)
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
//                   Docker Login to Google Container Registry
// ========================================================================== //

// pass GCP_SERVICE_ACCOUNT key and GCR_REGISTRY as the first and second args or it'll look for them in the environment

// must be called after gcpActivateServiceAccount.groovy
// must have GCloud SDK in the calling environment or will fall back to attempting a direct Docker login

def call (key='', registry='') {
  key = key ?: env.GCP_SERVICEACCOUNT_KEY ?: error('dockerLoginGCR: key not specified and GCP_SERVICEACCOUNT_KEY not set in the environment')
  registry = registry ?: env.GCR_REGISTRY ?: error('dockerLoginGCR: registry not specified and GCR_REGISTRY not set in the environment')
  withEnv(["GCP_SERVICEACCOUNT_KEY=$key", "GCR_REGISTRY=$registry"]) {
    script {
      if (isCommandAvailable('gcloud')) {
        echo 'Using GCloud SDK to configure Docker'
        // configures docker config with a token
        sh 'gcloud auth configure-docker "$GCR_REGISTRY"'
      } else {
        echo 'GCloud SDK is not installed, attempting to login with docker directly'
        if (!env.GCR_REGISTRY) {
          error('GCR_REGISTRY environment variable not set!')
        }
        if (!env.GCP_SERVICEACCOUNT_KEY) {
          error('GCP_SERVICEACCOUNT_KEY environment variable not set!')
        }
        dockerLogin('_json_key', env.GCP_SERVICEACCOUNT_KEY.bytes.decodeBase64().toString(), env.GCR_REGISTRY)
      }
    }
  }
}
