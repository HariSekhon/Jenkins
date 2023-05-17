//
//  Author: Hari Sekhon
//  Date: 2023-02-17 19:16:24 +0000 (Fri, 17 Feb 2023)
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
//                   G i t H u b   C r e a t e   R e l e a s e
// ========================================================================== //

// Creates a GitHub Release Tag of the given argument
//
// If no release tag is given, tries to auto-determine the next increment from the current latest release tag using adjacent function githubNextRelease.groovy - see there for more details
//
// Requires GITHUB_TOKEN credential to be defined in the environment

// https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release

def call (Map args = [ repo: '', release: '', targetRef: '' ]) {

  // if repo not given, assume own repo
  String ownerRepo = args.repo ?: gitOwnerRepo()

  String release = args.release ?: githubNextRelease()

  String targetRef = args.targetRef ?: env.GIT_COMMIT

  if (!targetRef?.trim()) {
    error 'No targetRef passed and could not find GIT_COMMIT environment variable'
  }

  echo "Creating GitHub repo '$ownerRepo' release: $release"

  sh (
    label: 'Create GitHub Release',
    script: """
      set -eu
      # set -x is useful for debugging but could expose your GITHUB_TOKEN in the logs, although Jenkins should redact it
      set -x
      #set -o pipefail 2>/dev/null || :
      curl -sSf \
        -X POST \
        -H "Accept: application/vnd.github+json" \
        -H "Authorization: Bearer \$GITHUB_TOKEN"\
        -H "X-GitHub-Api-Version: 2022-11-28" \
        https://api.github.com/repos/$ownerRepo/releases \
        -d '{
              "tag_name":"$release",
              "target_commitish": "$targetRef",
              "name": "$release",
              "body": "Jenkins auto-incremented release",
              "draft": false,
              "prerelease": false,
              "generate_release_notes": false
            }'
    """
  )

}
