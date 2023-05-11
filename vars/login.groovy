//
//  Author: Hari Sekhon
//  Date: 2022-06-20 16:59:19 +0100 (Mon, 20 Jun 2022)
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
//                                  L o g i n s
// ========================================================================== //

// Logs in to any cloud platforms for which environment variable credentials are available
//
// Adapted from 'login.sh' in DevOps Bash tools repo:
//
//  https://github.com/HariSekhon/DevOps-Bash-tools

// Currently supports:
//
//   - AWS
//   - GCP
//   - GitHub CLI
//   - Docker Registries:
//     - DockerHub
//     - GitHub Container Registry (GHCR)
//     - Gitlab Container Registry
//     - AWS Elastic Container Registry (ECR)
//     - Azure Container Registry (ACR)
//     - Google Container Registry (GCR)
//     - Google Artifact Registry (GAR)
//     - Quay.io Container Registry (quay)

// Usage in Jenkinsfile:
//
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // run to login to any platforms for which we have standard expected environment variables available
//
//      login()

def call () {
  echo 'Running Logins for any platforms we have environment credentials for'

  script {

    if (env.DOCKERHUB_USER && env.DOCKERHUB_TOKEN) {
      dockerLogin()
    }

    if (env.GH_TOKEN || env.GITHUB_TOKEN) {
      if (env.GITHUB_USER && isDockerAvailable()) {
        dockerLoginGHCR()
      }
      if (isCommandAvailable('gh')) {
        sh (
          label: 'GitHub CLI Auth Status',
          script: 'gh auth status'
        )
      }
    }

    if (env.AWS_ACCESS_KEY_ID &&
       env.AWS_SECRET_ACCESS_KEY &&
       isCommandAvailable('aws')) {
      awsAuth()
      if (isDockerAvailable()) {
        dockerLoginECR()
      }
    }

    if (env.GCP_SERVICEACCOUNT_KEY) {
      if (isCommandAvailable('gcloud')) {
        gcpActivateServiceAccount()
      }
      if (isDockerAvailable()) {
        if (env.GAR_REGISTRY) {
          dockerLoginGAR()
        }
        if (env.GCR_REGISTRY) {
          dockerLoginGCR()
        }
      }
      if (env.GOOGLE_APPLICATION_CREDENTIALS) {
        gcpSetupApplicationCredentials()
      }
    }

    if (env.AZURE_USER && env.AZURE_PASSWORD) {
      if (isCommandAvailable('az')) {
        azureCLILogin()
        if (isDockerAvailable()) {
          dockerLoginACR()
        }
      }
    }

    if (env.GITLAB_USER && env.GITLAB_TOKEN) {
      if (isDockerAvailable()) {
        dockerLoginGitlab()
      }
    }

    if (env.QUAY_USER && env.QUAY_TOKEN) {
      if (isDockerAvailable()) {
        dockerLoginQuay()
      }
    }

  }
}
