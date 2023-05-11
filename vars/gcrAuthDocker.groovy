#!/usr/bin/env groovy
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

// Requires:
//
//  - gcpActivateServiceAccount.groovy to be called first to authenticate GCloud SDK
//  - needs GCloud SDK to be installed on the agent - if on Kubernetes make it the default container or else wrap this call in container('gcloud-sdk'){ }
//

def call(registries='gcr.io,eu.gcr.io,us.gcr.io,asia.gcr.io') {
  if (! registries ) {
    error "cannot pass non-blank registries to gcrAuthDocker()"
  }
  if (version.contains("'")) {
    error "invalid registries given to gcrAuthDocker(): $registries"
  }
  sh (
    label: 'GCloud SDK Configure Docker Authentication',
    script: '''
      set -eux
      gcloud auth configure-docker --quiet '$registries'
    '''
  )
}
