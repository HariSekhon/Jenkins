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
//                         G i t   B r a n c h   L i s t
// ========================================================================== //

// Returns a list of strings of all git branches in the current git repo checkout
//
// By default returns only remote branches as this is usually what you want
//
// Assumes it's executing from inside a git cloned checkout
//
// Pass in a first boolean arg of 'true' to return local branches as well as remotes
//
// Requires 'git' to be in the $PATH

def call (all=false) {
  branches = sh (
      label: 'Git Branches',
      returnStdOut: true,
      script: """
        set -eux
        git branch --list -r
      """
  )
  if ( ! branches ) {
    error('No branches returns from git command in function gitBranchList()')
  }
  branchList = branches.
                split().
                collect {
                  it.split('/')[-1]
                }.findAll(
                  {
                    it != 'HEAD' &&
                    it != '->'
                  }
                ).unique()
  return branchList
}
