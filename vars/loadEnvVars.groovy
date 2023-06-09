//
//  Author: Hari Sekhon
//  Date: 2023-05-19 00:43:12 +0100 (Fri, 19 May 2023)
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
//              L o a d   E n v i r o n m e n t   V a r i a b l e s
// ========================================================================== //

// Takes a Map argument of environment variables and loads it into the environment to be used later in the pipeline
//
// Written for the Setup() stage of templated pipelines where you need to load environment variables only passed in to some pipelines
//
// Usage:
//
//    loadEnvVars(['NO_NOTIFY': 'true'])

def call(envVars) {
  if (! envVars) {
    return
  }
  if (envVars instanceof Map == false) {
    error 'envVars passed to loadEnvVars() must be a Map'
  }
  envVars.each { k, v ->
    env[k] = v
  }
}
