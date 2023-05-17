//
//  Author: Hari Sekhon
//  Date: 2022-07-19 14:32:02 +0100 (Tue, 19 Jul 2022)
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
//                          P i p e n v   I n s t a l l
// ========================================================================== //

// Sets up pipenv and installs the given package
//
// Must be run in an agent or container with 'pipenv' available such as 'kennethreitz/pipenv:latest' in kubernetes jenkins-agent-pod.yaml
//
// Usage:
//
//  container('pipenv') {
//    pipenvInstall('checkov')
//    sh 'pipenv run checkov ...'
//  }
//

def call (pipPackage) {
  withEnv(["PACKAGE=$pipPackage"]) {
    sh (
      label: 'Pipenv Install',
      script: '''
        set -eux
        pipenv install
        pipenv run pip install $PACKAGE
      '''
    )
  }
}
