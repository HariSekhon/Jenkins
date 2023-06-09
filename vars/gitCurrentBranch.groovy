//
//  Author: Hari Sekhon
//  Date: 2023-03-31 01:12:44 +0100 (Fri, 31 Mar 2023)
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
//                      G i t   C u r r e n t   B r a n c h
// ========================================================================== //

// Returns a string of the current branch name
//
// Assumes it's executing from inside a git cloned checkout
//
// Requires 'git' to be in the $PATH

def call () {
  currentBranch = sh (
      label: 'Git Current Branch',
      returnStdout: true,
      script: """
        set -eux
        git rev-parse --abbrev-ref HEAD
      """
  ).trim()
  if ( ! currentBranch ) {
    error('Failed to determine Git current branch in function gitCurrentBranch()')
  }
  return currentBranch
}
