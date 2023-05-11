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
//                 Docker Login to AWS Elastic Container Registry
// ========================================================================== //

// AWS CLI must be available in the environment and the following environment variables must be set:
//
//    AWS_ACCESS_KEY_ID
//    AWS_SECRET_ACCESS_KEY
//    AWS_DEFAULT_REGION
//    AWS_ACCOUNT_ID  // should be set by awsAuth() if jq is available

def call () {
  env.AWS_ACCOUNT_ID ?: error('dockerLoginECR: AWS_ACCOUNT_ID not set in the environment')
  env.AWS_DEFAULT_REGION ?: error('dockerLoginECR: AWS_DEFAULT_REGION not set in the environment')
  ECR_TOKEN = sh (
                label: 'Generating ECR Authentication Token',
                returnStdout: true,
                script: 'aws ecr get-login-password --region "$AWS_DEFAULT_REGION"'
              )
  script {
    if (! ECR_TOKEN) {
      error('dockerLoginECR: Failed to generate ECR_TOKEN')
    }
  }
  dockerLogin('AWS', ECR_TOKEN, "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com")
}
