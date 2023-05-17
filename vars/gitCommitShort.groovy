//
//  Author: Hari Sekhon
//  Date: 2023-02-22 14:53:55 +0000 (Wed, 22 Feb 2023)
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
//                           Generate GIT_COMMIT_SHORT
// ========================================================================== //

// Generates the GIT_COMMIT_SHORT environment variable from GIT_COMMIT and returns it
//
// If GIT_COMMIT is not set, attempts to also create that first from the Git Log
//
// Useful for auto-versioning dev builds to deploy automatically without generating too many release versions via githubCreateRelease.groovy for every git push
//
// https://www.jenkins.io/doc/pipeline/examples/#gitcommit

def call () {
  if ( ! env.GIT_COMMIT ) {
    echo "GIT_COMMIT environment variable not found, attempting to populate from git log"
    env.GIT_COMMIT = sh (
      returnStdout: true,
      script: "git log -n 1 --pretty=format:'%H'"
    ).trim()
    if ( ! env.GIT_COMMIT ) {
      error "Failed to determine GIT_COMMIT"
    }
  }
  int commitLength = env.GIT_COMMIT.length()
  if (commitLength != 40) {
    error "GIT_COMMIT environment variable is of unexpected length, expected 40 characters, got '$commitLength' characters in '$GIT_COMMIT'"
  }
  // avoid this call if GIT_COMMIT is already set
  //env.GIT_COMMIT_SHORT = sh (returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
  env.GIT_COMMIT_SHORT = env.GIT_COMMIT.substring(0, 7)
  return env.GIT_COMMIT_SHORT
}
