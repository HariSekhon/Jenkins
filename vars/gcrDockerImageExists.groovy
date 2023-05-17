//
//  Author: Hari Sekhon
//  Date: 2023-03-09 16:51:25 +0000 (Thu, 09 Mar 2023)
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
//                 G C R   D o c k e r   I m a g e   E x i s t s
// ========================================================================== //

// Checks if the given Docker Image GCR registry path and commit already exists
//
// Requires GCloud SDK CLI to be installed and authenticated

def call (String dockerImageRegistryPath, String dockerImageTag) {
  echo "Checking docker image '$dockerImageRegistryPath' tag '$dockerImageTag' exists"
  if (dockerImageRegistryPath.contains(':')) {
    dockerImageRegistryPath = dockerImageRegistryPath.split(':')[0]
  }
  String stdout = sh (
    // GCloud SDK returns zero whether found or not, so check the stdout for content
    returnStdout: true,
    label: "GCloud list tags",
    script: """
      set -eux
      gcloud container images list-tags '$dockerImageRegistryPath' --filter='tags ~ ^$dockerImageTag\$' --format=text
    """
    // XXX: do not do --filter='tags:$dockerImageTag' - this will match a partial substring such that an image
    //      with only a long hashref tag will be matched by a check for the short hashref, defeating adjacent gcrTagGitCommitShort.groovy
  )
  if (stdout) {
    echo "Docker image '$dockerImageRegistryPath:$dockerImageTag' exists"
    return true
  } else {
    echo "Docker image '$dockerImageRegistryPath:$dockerImageTag' does not exist"
    return false
  }
}
