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
//                         T e r r a f o r m   A p p l y
// ========================================================================== //

def call (timeoutMinutes=60) {
  String terraformDir = env.TERRAFORM_DIR ?: '.'
  String unique = "Dir: $terraformDir"
  String label = "Terraform Apply - $unique"
  // must differentiate lock to share the same lock between Terraform Plan and Terraform Apply
  String lockString = "Terraform - $unique"
  echo "Acquiring Terraform Apply Lock: $lockString"
  lock (resource: lockString, inversePrecedence: true) {
    // forbids older applys from starting
    milestone(ordinal: null, label: "Milestone: $label")

    // terraform docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
    //container('terraform') {
      timeout (time: timeoutMinutes, unit: 'MINUTES') {
        //dir ("components/${COMPONENT}") {
        ansiColor('xterm') {
          // for test environments, add a param to trigger -destroy switch
          dir("$terraformDir") {
            echo "$label"
            sh (
              label: "$label",
              // cannot add ${args} here when using saved plan, otherwise will get an error like this:
              //
              //[2022-07-11T15:45:39.085Z] + terraform apply -input=false -auto-approve -var-file ../tf_vars/datadog.tfvars plan.zip
              //[2022-07-11T15:45:39.085Z] ╷
              //[2022-07-11T15:45:39.085Z] │ Error: Can't set variables when applying a saved plan
              //[2022-07-11T15:45:39.085Z] │
              //[2022-07-11T15:45:39.085Z] │ The -var and -var-file options cannot be used when applying a saved plan
              //[2022-07-11T15:45:39.085Z] │ file, because a saved plan includes the variable values that were set when
              //[2022-07-11T15:45:39.085Z] │ it was created.
              //[2022-07-11T15:45:39.085Z] ╵
              //script returned exit code 1
              script: "terraform apply -input=false -auto-approve plan.zip"
            )
          }
        }
      }
    //}
  }
}
