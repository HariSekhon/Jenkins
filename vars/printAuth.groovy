//
//  Author: Hari Sekhon
//  Date: 2022-06-20 16:59:19 +0100 (Mon, 20 Jun 2022)
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
//                       P r i n t   A u t h   S t a t u s
// ========================================================================== //

// Shows the current auth status, who you're logged in as for the major Cloud platforms
//
// Usage in Jenkinsfile:
//
//    // import this library directly from github:
//
//      @Library('github.com/harisekhon/jenkins@master') _
//
//    // run to login to any platforms for which we have standard expected environment variables available
//
//      printAuth()

def call () {
  timeout (time: 2, unit: 'MINUTES') {
    echo 'Showing logged in status for any platforms we have CLIs available for'

    sh (
      label: 'Auth Status',
      script: '''
        set -eu

        whoami
        echo

        if command -v aws >/dev/null 2>&1; then
          echo "AWS:"
          aws sts get-caller-identity || :
          echo
        fi

        if command -v gcloud >/dev/null 2>&1; then
          echo "GCP:"
          gcloud auth list || :
          echo
        fi

        if command -v az >/dev/null 2>&1; then
          echo "Azure:"
          az ad signed-in-user show || :
          echo
        fi

        if command -v gh >/dev/null 2>&1; then
          gh auth status || :
          #echo  # above command prints an extra newline anyway
        fi

        if command -v docker >/dev/null 2>&1; then
          if [ -f ~/.docker/config.json ]; then
            if command -v jq >/dev/null 2>&1; then
              echo "Docker registries logged in to:"
              echo
              jq -r '.auths | keys[]' ~/.docker/config.json
              echo
            fi
          fi
        fi

      '''
    )
  }
}
