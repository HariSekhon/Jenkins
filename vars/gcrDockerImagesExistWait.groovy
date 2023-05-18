//
//  Author: Hari Sekhon
//  Date: 2023-03-31 00:24:09 +0100 (Fri, 31 Mar 2023)
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
//            G C R   D o c k e r   I m a g e s   E x i s t   W a i t
// ========================================================================== //

// Waits until all the given GCR docker image:tag paths exist for the specifed length of time in minutes
//
// Useful if you've got CloudBuild triggering asynchronously and Jenkins is waiting for them before continuing with a deployment
//
// Requires GCloud SDK CLI to be installed and authenticated

def call (List<String> dockerImageRegistryPaths=[], String dockerTag='', int waitMinutes=10) {
  dockerImageRegistryPaths = dockerImageRegistryPaths ?: dockerInferImageTagList()
  timeout (time: waitMinutes, unit: 'MINUTES') {
    echo "Waiting for $waitMinutes minutes for GCR docker images to become available"
    waitUntil {
      for (String dockerImageRegistryPath in dockerImageRegistryPaths) {
        String tag = dockerTag
        if (dockerImageRegistryPath.contains(':')) {
          tag = dockerImageRegistryPath.split(':')[-1]
        }
        if (!tag) {
          error("gcrDockerImagesExistWait() passed docker image registry path without a tag suffix, which will usually be true after even 1 build: $dockerImageRegistryPath")
        }
        if (!gcrDockerImageExists(dockerImageRegistryPath, tag)) {
          sleep(
            time: 10,
            unit: 'SECONDS'  // default is SECONDS
          )
          return false
        }
      }
      return true
    }
  }
}
