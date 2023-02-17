#!/usr/bin/env groovy
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
// Requires GITHUB_TOKEN credential to be defined in the environment

def call(String repo='') {

  String repo_url = env.GIT_URL

  // strip any git@github.com: prefix
  String owner_repo = repo_url.split(':')[-1]

  // take the last two components from "https://github.com/<owner>/<repo>" to leave just "<owner>/<repo>"
  owner_repo = owner_repo.split('/')[-2,-1].join('/')

  // if repo not given, assume own repo
  owner_repo = repo ?: owner_repo

  echo "Querying GitHub API for latest release tag of repo: $owner_repo"

  String json_output = sh(
    returnStdout: true,
		label: 'Query GitHub Releases',
    script: """
      set -eux
      set -o pipefail 2>/dev/null || :
      curl -H "Authorization: Bearer \$GITHUB_TOKEN" "https://api.github.com/repos/$owner_repo/releases/latest"
    """
  )

  def json = new groovy.json.JsonSlurper().parseText(json_output)
  assert json instanceof Map

  String currentVersion = json.tag_name

  List version_components = currentVersion.findAll( /\d+/ ).collect{ it.toInteger() }

  echo "Latest release is: ${version_components.join('.')}"

  // increment the last number by one
  version_components[-1] = version_components[-1] + 1

  newRelease = version_components.join('.')

  echo "New release is: $newVersion"

  return newVersion

}
