#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-02-01 18:56:45 +0000 (Tue, 01 Feb 2022)
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

def call() {
  sh (
    label: 'Download KICS',
    script: '''#!/bin/bash
      set -euxo pipefail
      curl -sSf https://api.github.com/repos/Checkmarx/kics/releases/latest |
      jq -r '
        .assets[] |
        select(
          .browser_download_url |
          test("linux_x64.tar.gz")
        ) |
        .browser_download_url
      ' |
      head -n1 |
      while read -r url; do
          wget -qc "$url" -O /tmp/kics.tar.gz
          tar zxvf /tmp/kics.tar.gz -C /usr/local/bin
          rm -fv /tmp/kics.tar.gz
      done
      /usr/local/bin/kics version
    '''
  )
}
