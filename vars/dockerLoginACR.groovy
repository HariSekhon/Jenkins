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
//                    Docker Login to Azure Container Registry
// ========================================================================== //

// must be called after 'az login' and have Azure CLI in the calling environment
//
// $ACR_REGISTRY_NAME must be passed as the first argument or else set in the calling environment

def call (registry='') {
  registry = registry ?: env.ACR_REGISTRY_NAME ?: error('dockerLoginACR: Azure registry name not specified and ACR_REGISTRY_NAME not set in the environment')
  withEnv(["ACR_REGISTRY_NAME=$registry"]) {
    script {
      // configures docker config with a token
      sh '''
        set -eux
        az acr login --name "$ACR_REGISRY_NAME"
      '''
    }
  }
}
