//
//  Author: Hari Sekhon
//  Date: 2022-01-06 17:35:16 +0000 (Thu, 06 Jan 2022)
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
//                        T e r r a g r u n t   A p p l y
// ========================================================================== //

def call (timeoutMinutes=30) {
  String terraformDir = env.TERRAFORM_DIR ?: '.'
  String unique = "Dir: $terraformDir"
  String label = "Terragrunt Apply - $unique"
  // must differentiate lock to share the same lock between Terraform Plan and Terraform Apply
  String lockString  = "Terraform - $unique"
  echo "Acquiring Terragrunt Apply Lock: $lockString"
  lock (resource: lockString, inversePrecedence: true) {  // use same lock between Terraform / Terragrunt for safety
    // forbids older applys from starting
    milestone(ordinal: null, label: "Milestone: $label")

    // terragrunt docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
    //container('terragrunt') {
      timeout (time: timeoutMinutes, unit: 'MINUTES') {
        //dir ("components/${COMPONENT}") {
        ansiColor('xterm') {
          // for test environments, add a param to trigger -destroy switch
          dir("$terraformDir") {
            echo "$label"
            sh (
              label: "$label",
              script: 'terragrunt apply plan.zip --terragrunt-non-interactive -input=false -auto-approve'
            )
          }
        }
      }
    //}
  }
}
