//
//  Author: Hari Sekhon
//  Date: 2023-05-11 17:58:49 +0100 (Thu, 11 May 2023)
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
//                    Configure Docker to Authenticate to GCR
// ========================================================================== //

// Configures Docker for both Google Container Registry and the newer Google Artifact Registry

// Requires:
//
//  - gcpActivateServiceAccount.groovy to be called first to authenticate GCloud SDK
//  - needs GCloud SDK to be installed on the agent - if on Kubernetes make it the default container or else wrap this call in container('gcloud-sdk') { }
//

// if passing blank for GAR registries, garAuthDocker() will auto-determine all registries
// if passing blank for GCR registries, will use hardcoded default list taken from documentation
def call (garRegistries='', gcrRegistries='') {
  garDockerAuth(garRegistries)
  gcrDockerAuth(gcrRegistries)
}
