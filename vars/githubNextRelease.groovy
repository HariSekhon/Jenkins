//
//  Author: Hari Sekhon
//  Date: 2023-02-17 18:27:43 +0000 (Fri, 17 Feb 2023)
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
//                     G i t H u b   N e x t   R e l e a s e
// ========================================================================== //

// Returns the next release version by querying current GitHub releases and incrementing by one
//
// Expects release version format such as semver 1.2.3 or YYYY.NN eg. 2023.1
//
// XXX: does not currently support alphabetic naming convention prefixes/suffixes like v1.0 or kustomize/v5.0.0
//
// Requires GITHUB_TOKEN credential to be defined in the environment

// https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#get-the-latest-release

def call (String repo='') {

  // if repo not given, assume own repo
  String ownerRepo = repo ?: gitOwnerRepo()

  echo "Querying GitHub API for latest release tag of repo: $ownerRepo"

  String jsonOutput = sh (
    returnStdout: true,
    label: 'Query GitHub Latest Release',
    script: """
      set -eu
      # set -x is useful for debugging but could expose your GITHUB_TOKEN in the logs, although Jenkins should redact it
      set -x
      #set -o pipefail 2>/dev/null || :
      curl -sS -H "Authorization: Bearer \$GITHUB_TOKEN" "https://api.github.com/repos/$ownerRepo/releases/latest"
    """
  )

  def json = new groovy.json.JsonSlurper().parseText(jsonOutput)
  assert json instanceof Map

  String currentVersion = json.tag_name

  // XXX: GitHub json.tag_name is a string and so this won't carry through any alphabetic naming conventions prefixes/suffixes such a v1.0 or kustomize/v5.0.0 - something to be improved
  //List versionComponents = currentVersion.findAll( /\d+/ ).collect { it.toInteger() }
  List versionComponents = currentVersion.findAll( /\d+/ )*.toInteger()

  echo "Latest release is: ${versionComponents.join('.')}"

  // increment the last number by one
  versionComponents[-1] = versionComponents[-1] + 1

  newVersion = versionComponents.join('.')

  echo "New release is: $newVersion"

  return newVersion

}
