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

def call(timeoutMinutes=1){
  String label = 'Terraform Fmt'

  // forbids older inits from starting
  //milestone(ordinal: 1, label: "Milestone: $label")
  milestone(label: "Milestone: $label")

  // XXX: set Terraform version in the docker image tag in jenkins-agent-pod.yaml
  container('terraform') {
    timeout(time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        dir(System.getenv("TERRAFORM_DIR") ?: ".") {
          echo 'Terraform Fmt'
          echo "$label"
          sh (
            label: "$label",
            script: 'terraform fmt'
          )
        }
      }
    }
  }
}
