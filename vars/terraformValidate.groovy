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
//                      T e r r a f o r m   V a l i d a t e
// ========================================================================== //

// Validates your Terraform code
//
// Used in terraformPipeline.groovy

def call (timeoutMinutes=2) {
  String label = 'Terraform Validate'

  // forbids older inits from starting
  milestone(ordinal: null, label: "Milestone: $label")

  // terraform docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
  //container('terraform') {
    timeout (time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        dir(env.TERRAFORM_DIR ?: ".") {
          echo 'Terraform Validate'
          echo "$label"
          sh (
            label: "$label",
            script: 'terraform validate'
          )
        }
      }
    }
  //}
}
