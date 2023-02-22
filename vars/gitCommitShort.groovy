#!/usr/bin/env groovy
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

// Generates the GIT_COMMIT_SHORT environment variable and returns it
//
// Useful for auto-versioning dev builds to deploy automatically without generating too many release versions via githubCreateRelease.groovy for every git push
//
// https://www.jenkins.io/doc/pipeline/examples/#gitcommit

def call() {
    env.GIT_COMMIT_SHORT = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
    return env.GIT_COMMIT_SHORT
}
