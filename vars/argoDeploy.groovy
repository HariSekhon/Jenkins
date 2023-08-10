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
//                           A r g o C D   D e p l o y
// ========================================================================== //

// Required Environment Variables to be set in environment{} section of Jenkinsfile, see top level Jenkinsfile template
//
//    ARGOCD_SERVER
//    ARGOCD_AUTH_TOKEN
//
// The ArgoCD app must be passed as the first argument

def call (app, timeoutMinutes=20) {
  String label = "ArgoCD Deploy - App: '$app'"
  int numRetries = 2
  int minMinutes = 3
  if(timeoutMinutes < minMinutes){
    error("Cannot set timeoutMinutes arg < $minMinutes in argoDeploy() function")
  }
  int timeoutSeconds = ( timeoutMinutes * 60 / (numRetries + 1) ) - 10
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

        syncTimeoutMinutes = Math.max(1, Math.ceil(timeoutSeconds / 60).toInteger() )

        argoSync("$app", syncTimeoutMinutes)

        // results in a timeout status, but prefer an error status to bubble up instead
        //waitUntil (initialRecurrencePeriod: 5000) {
        retry(numRetries) {
          withEnv (["APP=$app", "TIMEOUT_SECONDS=$timeoutSeconds"]) {
            //echo "$label"
            script {
              //int exitCode = sh (
              sh (
                label: "$label",
                //returnStatus: true,
                //
                // tried argocd --hard-refresh to work around this problem:
                //
                //   Message:            ComparisonError: rpc error: code = Unknown desc = Manifest generation error (cached): `kustomize build /tmp/git@github.com_MYORG_kubernetes/www/production --enable-helm` failed timeout after 1m30s
                //
                // it seemed to work initially when run as a one off in the UI,
                // but when applied in CI/CD and run every time to try to patch over that problem,
                // it resulted in performance issues and 504 gateway timeouts to ArgoCD (via an ingress)
                //
                script: '''
                  set -eux

                  # might cause performance issues and 504 timeouts
                  #
                  #   https://github.com/argoproj/argo-cd/issues/9701
                  #
                  #argocd app get  "$APP" --grpc-web --hard-refresh
                  #argocd app wait "$APP" --grpc-web --timeout "$TIMEOUT_SECONDS" || :

                  # workaround for issue:
                  #
                  #   https://github.com/argoproj/argo-cd/issues/5592
                  #
                  #argocd --grpc-web app get "$APP" --refresh > /dev/null

                  # needed in case auto-sync isn't enabled - above workaround isn't enough
                  # done in argoSync() function now to be able to parallelize app deployments by pre-calling Sync
                  #argocd app sync "$APP" --grpc-web --force

                  # workaround for issue:
                  #
                  #   https://github.com/argoproj/argo-cd/issues/6013
                  #
                  #argocd app wait "$APP" --sync      --grpc-web --timeout "$TIMEOUT_SECONDS"
                  #argocd app wait "$APP" --operation --grpc-web --timeout "$TIMEOUT_SECONDS"
                  # HPAs transition to degraded, causing deployments to fail even on retry
                  ##argocd app wait "$APP" --health    --grpc-web --timeout "$TIMEOUT_SECONDS"
                  #
                  # is no good because not running --health checks returns ok on the deployment in Jenkins pipeine when k8s pods are actually failing healthchecks

                  argocd app wait "$APP" --sync --operation --health --grpc-web --timeout "$TIMEOUT_SECONDS"
                '''
              )
              // convert exitCode boolean for waitUntil()
              //exitCode == 0
            }
          }
        }
      }
    //}
  }
}
