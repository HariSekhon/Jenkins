//
//  Author: Hari Sekhon
//  Date: 2021-04-30 15:25:01 +0100 (Fri, 30 Apr 2021)
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
//                           S o l r   R e - I n d e x
// ========================================================================== //

// Run the given script to do a Solr re-index, locking it so that no other script or deployment will clash with it

// Required Environment Variables to be set in environment{} section of Jenkinsfile, see top level Jenkinsfile template
//
//    APP
//    ENVIRONMENT

def call (String scriptPath, int timeoutMinutes=60) {
  String app = env.APP.toLowerCase()
  String environment = env.ENVIRONMENT.toLowerCase()
  String Environment = env.ENVIRONMENT.capitalize()
  echo "Solr Re-Indexing App '$APP' '$Environment' from branch '$GIT_BRANCH'"
  // XXX: these locks must match whatever locks are used by your deployment functions, in this case gitKustomizeImages() and argoDeploy()
  String gitKustomizeLock = "Git Kustomize Image Version - Dir: '$app/$environment'"
  String deploymentLock   = "ArgoCD Deploy - App: '$app-$environment'"
  String indexingLock     = "Solr Re-Indexing - App: '$APP', Environment: $Environment"

  scriptLockExecute(scriptPath, [gitKustomizeLock, deploymentLock, indexingLock], timeoutMinutes)
}
