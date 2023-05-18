//
//  Author: Hari Sekhon
//  Date: 2023-05-11 17:58:49 +0100 (Thu, 11 May 2023)
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
//        Configure Docker to Authenticate to Google Artifact Registry
// ========================================================================== //

// Requires:
//
//  - gcpActivateServiceAccount.groovy to be called first to authenticate GCloud SDK
//  - needs GCloud SDK to be installed on the agent - if on Kubernetes make it the default container or else wrap this call in container('gcloud-sdk') { }
//
//  XXX: GCloud SDK fails without throwing non-zero exit code for invalid registries with:
//
//            WARNING: blah is not a supported registry

def call (registries='') {
  if (!registries) {
    echo "No GAR registries specified, auto-populating complete GAR registry list"
    registries = sh (
      label: 'GCloud SDK fetch GAR registry locations',
      returnStdout: true,
      script: """
        set -eux
        if [ -n "\${GAR_PROJECT:-}" ]; then
          export CLOUDSDK_CORE_PROJECT="\$GAR_PROJECT"
        fi
        gcloud artifacts locations list --format='get(name)' | tr '\\n' ',' | sed 's/,\$//'
      """
    )
    if (!registries) {
      error "Failed to get list of GAR registry locations"
    }
    registries = registries.split(',').collect { "${it}-docker.pkg.dev" }.join(',')
  }
  if (registries.contains("'")) {
    error "invalid registries given to garAuthDocker(): $registries"
  }
  sh (
    label: 'GCloud SDK Configure Docker Authentication for Google Artifact Registry',
    script: """
      set -eux
      if [ -n "\${GAR_PROJECT:-}" ]; then
        export CLOUDSDK_CORE_PROJECT="\$GCR_PROJECT"
      fi
      # XXX: fails without throwing non-zero exit code for invalid registries with
      #      WARNING: blah is not a supported registry
      gcloud auth configure-docker --quiet '$registries'
    """
  )
}
