//
//  Author: Hari Sekhon
//  Date: 2021-09-01 14:07:59 +0100 (Wed, 01 Sep 2021)
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

// Required Environment Variables to be set in environment{} section of Jenkinsfile, see top level Jenkinsfile template
//
//    APP
//    ENVIRONMENT
//    ARGOCD_SERVER
//    ARGOCD_AUTH_TOKEN
//
// The ArgoCD app must be set up with a name of $APP-$ENVIRONMENT

def call(timeoutMinutes=10){
  String label = "Deploying ArgoCD - App '$APP', Environment: " + "$ENVIRONMENT".capitalize()
  milestone ordinal: 100, label: "Milestone: $label"
  int timeoutSeconds = timeoutMinutes * 60
  echo "Acquiring ArgoCD Lock: $label"
  lock(resource: label, inversePrecedence: true){
    container('argocd') {
      timeout(time: timeoutMinutes, unit: 'MINUTES') {
        withEnv(["TIMEOUT_SECONDS=$timeoutSeconds"]) {
          echo "$label"
          sh (
            label: "$label",
            script: '''#!/bin/bash
              set -euxo pipefail
              argocd app get  "$APP-$ENVIRONMENT" --grpc-web --hard-refresh
              argocd app sync "$APP-$ENVIRONMENT" --grpc-web --force
              argocd app wait "$APP-$ENVIRONMENT" --grpc-web --timeout "$TIMEOUT_SECONDS"
            '''
          )
        }
      }
    }
  }
}
