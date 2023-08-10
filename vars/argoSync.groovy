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

// ========================================================================== //
//                             A r g o C D   S y n c
// ========================================================================== //

// Forked from argoDeploy() function to allow app deployment parallelization
// by pre-calling argoSync for more than one app before argoDeploy() which waits on each app

// Required Environment Variables to be set in environment{} section of Jenkinsfile, see top level Jenkinsfile template
//
//    ARGOCD_SERVER
//    ARGOCD_AUTH_TOKEN
//
// The ArgoCD app must be passed as the first argument

// ArgoCD sync usually takes 20-60 secs even for a large app full of different deployments and many cronjobs,
// so 5 minute timeout default should be more than enough for all sane use cases

def call (app, timeoutMinutes=5) {
  String label = "ArgoCD Sync - App: '$app'"
  int timeoutSeconds = timeoutMinutes * 60 - 10
  echo "Acquiring ArgoCD Lock: $label"
  lock (resource: label, inversePrecedence: true) {
    // XXX: prevents calling in a parallel stage otherwise you'll get this error:
    //
    //  "Using a milestone step inside parallel is not allowed"
    //
    milestone ordinal: null, label: "Milestone: $label"
    // let caller decide if wrapping this in a container('argocd') or using downloadArgo.groovy to save RAM
    //container ('argocd') {
      timeout (time: timeoutMinutes, unit: 'MINUTES') {
        waitUntil (initialRecurrencePeriod: 5000) {
          withEnv (["APP=$app", "TIMEOUT_SECONDS=$timeoutSeconds"]) {
            // Blue Ocean doesn't hide script with this label, even though it does for argoDeploy(), so echo explicitly
            echo "$label"
            script {
              int exitCode = sh (
                label: "$label",
                returnStatus: true,
                script: '''
                  set -eux

                  argocd app sync "$APP" --grpc-web --force --timeout "$TIMEOUT_SECONDS"
                '''
              )
              // convert exitCode boolean for waitUntil()
              exitCode == 0
            }
          }
        }
      }
    //}
  }
}
