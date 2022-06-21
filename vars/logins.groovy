#!/usr/bin/env groovy
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

// Usage in Jenkinsfile:
//
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // run to login to any plaforms for which we have standard expected environment variables available
//
//    login()

// Jenkins Shared Library function
def call(){
  echo 'Running Logins for any platforms we have environment credentials for'

  //stages {
  //
  //  stage('GCP Activate Service Account') {
  steps {
      when {
        beforeAgent true
        not {
          // must match the env var used in the gcpActivateServiceAccount() function
          environment name: 'GCP_SERVICEACCOUNT_KEY', value: ''
        }
      }
      step {
        gcpActivateServiceAccount()
      }
    //}

    //stage('Docker Login DockerHub') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'DOCKERHUB_USER',  value: '' }
          not { environment name: 'DOCKERHUB_TOKEN', value: '' }
        }
      }
      step {
        dockerLogin()
      }
    //}

    //stage('Docker Login GitHub Container Registry') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'GITHUB_USER',  value: '' }
          not { environment name: 'GITHUB_TOKEN', value: '' }
        }
      }
      step {
        dockerLoginGHCR()
      }
    //}

    //stage('Docker Login GitLab Registry') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'GITLAB_USER',  value: '' }
          not { environment name: 'GITLAB_TOKEN', value: '' }
        }
      }
      step {
        dockerLoginGitlab()
      }
    //}

    //stage('Docker Login Azure Container Registry') {
      when {
        beforeAgent true
        allOf {
          // XXX: add auth function and check for their env vars
          not { environment name: 'ACR_NAME', value: '' }
        }
      }
      step {
        dockerLoginACR()
      }
    //}

    //stage('Docker Login AWS Elastic Container Registry') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'AWS_ACCESS_KEY_ID',     value: '' }
          not { environment name: 'AWS_SECRET_ACCESS_KEY', value: '' }
          not { environment name: 'AWS_DEFAULT_REGION',    value: '' }
        }
      }
      step {
        dockerLoginECR()
      }
    //}

    //stage('Docker Login AWS Google Artifact Registry') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'GCP_SERVICEACCOUNT_KEY', value: '' }
          not { environment name: 'GAR_REGISTRY', value: '' }
        }
      }
      step {
        dockerLoginGAR()
      }
    //}

    //stage('Docker Login AWS Google Container Registry') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'GCP_SERVICEACCOUNT_KEY', value: '' }
          not { environment name: 'GCR_REGISTRY', value: '' }
        }
      }
      step {
        dockerLoginGCR()
      }
    //}

    //stage('Docker Login AWS Quay.io Registry') {
      when {
        beforeAgent true
        allOf {
          not { environment name: 'QUAY_USER',  value: '' }
          not { environment name: 'QUAY_TOKEN', value: '' }
        }
      }
      step {
        dockerLoginQuay()
      }
    //}

  }

}
