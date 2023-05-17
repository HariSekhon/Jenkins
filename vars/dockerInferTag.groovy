//
//  Author: Hari Sekhon
//  Date: 2023-05-14 04:43:32 +0100 (Sun, 14 May 2023)
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
//                        D o c k e r   I n f e r   T a g
// ========================================================================== //

// Infers a Docker Image Tag from possible environment variables and returns it as a String

def call(tag='') {
  return tag ?:
         env.DOCKER_TAG ?:
         env.CONTAINER_TAG ?:
         env.VERSION ?:
         env.GIT_COMMIT ?:
         'latest'
}
