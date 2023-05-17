//
//  Author: Hari Sekhon
//  Date: 2023-05-16 04:35:38 +0100 (Tue, 16 May 2023)
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
// Generate DOCKER_IMAGES environment variable with full GCR registry prefixes
// ========================================================================== //

def call(images=[]) {
  if (! env.GCR_REGISTRY) {
    error('GCR_REGISTRY environment variable not set before calling gcrGenerateEnvVarDockerImages()')
  }
  if (! env.GCR_PROJECT) {
    error('GCR_PROJECT environment variable not set before calling gcrGenerateEnvVarDockerImages()')
  }

  return generateEnvVarDockerImages(images, "$GCR_REGISTRY/$GCR_PROJECT")
}
