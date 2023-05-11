//
//  Author: Hari Sekhon
//  Date: 2022-07-22 18:06:58 +0100 (Fri, 22 Jul 2022)
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
//                         D o w n l o a d   D a t r e e
// ========================================================================== //

//def call (version = '1.5.1') {
//  withEnv(["VERSION=${version}"]) {
def call () {
  installPackages(
    [
      'bash',
      'curl',
      'unzip'
    ]
  )
  String label = "Download Datree on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    sh (
      label: "$label",
      script: '''
        set -eux

        curl https://get.datree.io |
        /usr/bin/env bash
      '''
    )
    sh (
      label: "Datree Version",
      script: '''
        set -eu
        datree version
      '''
    )
  }
}
