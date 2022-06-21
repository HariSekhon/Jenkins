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

// $APP and $ENVIRONMENT must be set in pipeline to ensure separate locking

def call(timeoutMinutes=30){
  String label = "Terraform Apply - Dir: ${env.TERRAFORM_DIR ?: '.'}"
  // must differentiate lock because Terraform Plan and Terraform Apply must share the same lock
  String lock  = "Terraform - Dir: ${env.TERRAFORM_DIR ?: '.'}"
  echo "Acquiring Terraform Apply Lock: $lock"
  lock(resource: lock, inversePrecedence: true) {
    // forbids older applys from starting
    milestone(ordinal: null, label: "Milestone: $label")

    // terraform docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
    //container('terraform') {
      timeout(time: timeoutMinutes, unit: 'MINUTES') {
        //dir ("components/${COMPONENT}") {
        ansiColor('xterm') {
          // for test environments, add a param to trigger -destroy switch
          dir(env.TERRAFORM_DIR ?: ".") {
            echo "$label"
            sh (
              label: "$label",
              script: 'terraform apply plan.zip -input=false -auto-approve'
            )
          }
        }
      }
    //}
  }
}
