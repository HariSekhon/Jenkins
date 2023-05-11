//
//  Author: Hari Sekhon
//  Date: 2022-01-07 18:48:47 +0000 (Fri, 07 Jan 2022)
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
//                T e r r a f o r m   R e f r e s h   S t a t e s
// ========================================================================== //

// For large estates to run a separate refresh-only job periodically to keep the state file up to date
//
// $APP and $ENVIRONMENT must be set in pipeline to ensure separate locking

def call (timeoutMinutes=59) {
  String terraformDir = env.TERRAFORM_DIR ?: '.'
  String unique = "Dir: $terraformDir"
  String label = "Terraform Refresh State - $unique"
  // must differentiate lock to share the same lock between Terraform Plan and Terraform Apply
  String lockString = "Terraform - $unique"
  echo "Acquiring Terraform Refresh Lock: $lockString"
  lock (resource: lockString, inversePrecedence: true) {
    // forbids older runs from starting
    milestone(ordinal: null, label: "Milestone: $label")

    // terraform docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
    //container('terraform') {
      timeout (time: timeoutMinutes, unit: 'MINUTES') {
        //dir ("components/${COMPONENT}") {
        ansiColor('xterm') {
          // for test environments, add a param to trigger -destroy switch
          dir(env.TERRAFORM_DIR ?: ".") {
            echo "$label"
            sh (
              label: "$label",
              script: 'terraform apply -refresh-only -input=false'
            )
          }
        }
      }
    //}
  }
}
