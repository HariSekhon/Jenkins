#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2023-03-09 16:58:44 +0000 (Thu, 09 Mar 2023)
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
//                                  G r o o v y
// ========================================================================== //

// Tags a given GCR Docker Image registry path and commit with a new alternative tag
//
// Written for gcrTagGitCommitShort() to be able to create a convenience tag of short git commit

def call(String dockerImageRegistryPathTag, String newTag) {
  String dockerImageRegistryPath = dockerImageRegistryPathTag.split(':')[0]
  echo "Tagging docker image '$dockerImageRegistryPath' with new tag '$dockerImageTag'"
  sh (
    label: "GCloud add tag",
    script: """
      set -eux
      gcloud container images add-tag --quiet "$dockerImageRegistryPathTag" "$dockerImageRegistryPath":"$GIT_COMMIT_SHORT"
    """
  )
}
