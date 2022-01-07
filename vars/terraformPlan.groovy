//
//  Author: Hari Sekhon
//  Date: 2022-01-06 17:35:16 +0000 (Thu, 06 Jan 2022)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/templates
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

def call(timeoutMinutes=10){
  label 'Terraform Plan'
  // forbids older plans from starting
  milestone(ordinal: 50, label: "Milestone: Terraform Plan")

  // XXX: set Terraform version in the docker image tag in jenkins-agent-pod.yaml
  container('terraform') {
    timeout(time: timeoutMinutes, unit: 'MINUTES') {
      //dir ("components/${COMPONENT}") {
      ansiColor('xterm') {
        // terraform docker image doesn't have bash
        //sh '''#/usr/bin/env bash -euxo pipefail
        //sh '''#/bin/sh -eux
        sh label: 'Workspace List',
          script: 'terraform workspace list || : ' // 'workspaces not supported' if using Terraform Cloud as a backend
        sh label: 'Terraform Plan',
          script: 'terraform plan -out=plan.zip -input=false'  // # -var-file=base.tfvars -var-file="$ENV.tfvars"
      }
    }
  }
}