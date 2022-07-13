#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-05-23 14:00:54 +0100 (Mon, 23 May 2022)
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
//                                  D a t r e e
// ========================================================================== //

// https://hub.datree.io/cicd-examples/jenkins-pipeline
//
// You may want to wrap this in an error handled stage to surface the problems but not fail the pipeline:
//
//    stage('Datree Test') {
//      steps {
//        catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//          datree()
//        }
//      }
//    }
//

def call(Map args = [dir: '.', kustomize: false, args: '']) {
  // relies on recursive shell expansion, which doesn't work on older versions of Bash:
  //
  //  https://github.com/datreeio/datree/issues/618
  //
  //sh 'datree test **.y*ml --only-k8s-files

  String label = "Datree Test"

  milestone "$label"

  withEnv(["DIR=${args.dir ?: '.'}", "KUSTOMIZE=${args.kustomize && 'kustomize' || ''}", "ARGS=${args.args}"]){
    // needs to be bash for pipefail detection
    sh (
      label "$label",
      script: '''#!/usr/bin/env bash
        set -euxo pipefail

        find "$DIR" -type f -name '*.yaml' -o -type -f -name '*.yml' -print0 |
        xargs -0 datree $KUSTOMIZE test --only-k8s-files $ARGS
      '''
    )
  }
}
