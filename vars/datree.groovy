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
// Requirement: DATREE_TOKEN must be set in the environment
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

// if you set
//
//  kustomize: true
//  args: '-- --enable-helm'
//
// it'll show errors materialized from external helm charts
// you'll then end up spending your life writing patches for all the imperfections of upstream projects

def call (Map args = [dir: '.', kustomize: false, args: '']) {
  // relies on recursive shell expansion, which doesn't work on older versions of Bash:
  //
  //  https://github.com/datreeio/datree/issues/618
  //
  //sh 'datree test **/*.y*ml --only-k8s-files

  dir = args.dir ?: '.'
  kustomize = args.kustomize ?: 'false'
  args = args.args ?: ''

  boolean datreeExists = sh (
    label: 'Check Datree CLI available',
    returnStatus: true,
    script: 'command -v datree'
  ) == 0

  if (!datreeExists) {
    echo "Datree CLI not found, downloading..."
    downloadDatree()
  }

  String label = "Datree Test"
  if ("$kustomize" == "true") {
    label = "Datree Kustomize Test"
  }

  // XXX: prevents calling in a parallel stage otherwise you'll get this error:
  //
  //  "Using a milestone step inside parallel is not allowed"
  //
  milestone ordinal: null, label: "$label"

  withEnv(["DIR=$dir", "KUSTOMIZE=$kustomize", "ARGS=$args"]) {
    // needs to be bash for pipefail detection
    sh (
      label: "$label",
      script: '''#!/usr/bin/env bash
        set -euxo pipefail

        if [ "$KUSTOMIZE" = true ]; then
          find "$DIR" -type f -name 'kustomization.y*ml' |
          while read -r kustomization; do
            dir="$(dirname "$kustomization")"
            datree kustomize test "$dir" ${ARGS:-} || exit 1
            echo
          done
        else
          find "$DIR" -type f -iname '*.y*ml' -print0 |
          xargs -0 --no-run-if-empty \
            datree test --only-k8s-files ${ARGS:-}
        fi
      '''
    )
  }
}
