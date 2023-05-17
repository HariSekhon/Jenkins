//
//  Author: Hari Sekhon
//  Date: 2023-05-14 05:04:01 +0100 (Sun, 14 May 2023)
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
//                 D o c k e r   I n f e r   I m a g e   L i s t
// ========================================================================== //

// Constructs and returns a List of docker images without tags by inferring from environment variables

def call() {
  List images = []
  if (env.DOCKER_IMAGES) {
    images = env.DOCKER_IMAGES.split(',')
  } else if (env.DOCKER_IMAGES_TAGS) {
    for (docker_image in env.DOCKER_IMAGES_TAGS.split(',')) {
      images.add(docker.image.split(':')[0])
    }
  } else if (env.DOCKER_IMAGE) {
    images.add(env.DOCKER_IMAGE)
  } else if (env.DOCKER_IMAGE_TAG) {
      images.add(env.DOCKER_IMAGE.split(':'[0]))
  } else {
    error "Failed to infer docker images for list. None of the following environment variables were found: \$DOCKER_IMAGES, \$DOCKER_IMAGES_TAGS, \$DOCKER_IMAGE, \$DOCKER_IMAGE_TAG"
  }
  return images
}
