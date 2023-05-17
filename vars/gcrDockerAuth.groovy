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
//       Configure Docker to Authenticate to Google Container Registry
// ========================================================================== //

// Requires:
//
//  - gcpActivateServiceAccount.groovy to be called first to authenticate GCloud SDK
//  - needs GCloud SDK to be installed on the agent - if on Kubernetes make it the default container or else wrap this call in container('gcloud-sdk') { }
//
//  XXX: GCloud SDK fails without throwing non-zero exit code for invalid registries with:
//
//            WARNING: blah is not a supported registry

// registry list is from here: https://cloud.google.com/container-registry/docs/overview#registries
def call (registries='') {
  if (!registries) {
    registries = 'gcr.io,eu.gcr.io,us.gcr.io,asia.gcr.io'
    echo "No GCR registries specified, using default list of all documented regional registries: $registries"
    //error "cannot pass non-blank registries to gcrAuthDocker()"
    //
    // Can't find a GCloud SDK command similar to GAR to get a list of registries
    //echo "No GAR registries given, auto-populating complete GAR registry list"
    //registries = sh (
    //  label: 'GCloud SDK fetch GAR registries',
    //  returnStdout: true,
    //  script: "cloud artifacts locations list --format='get(name)' | tr '\\n' ',' | sed 's/,$//'"
    //)
  }
  if (registries.contains("'")) {
    error "invalid registries given to gcrAuthDocker(): $registries"
  }
  sh (
    label: 'GCloud SDK Configure Docker Authentication for Google Container Registry',
    script: """
      set -eux
      if [ -n "\${GCR_PROJECT:-}" ]; then
        export CLOUDSDK_CORE_PROJECT="\$GCR_PROJECT"
      fi
      # XXX: fails without throwing non-zero exit code for invalid registries with
      #      WARNING: blah is not a supported registry
      gcloud auth configure-docker --quiet '$registries'
    """
  )
}
