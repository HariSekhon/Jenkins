#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-06-21 10:46:51 +0100 (Tue, 21 Jun 2022)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/Jenkins
//
//  Liceese: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//                           Docker Login to DockerHub
// ========================================================================== //

// DOCKERHUB_USER and DOCKERHUB_TOKEN must be set in the calling environment

def call() {
  sh '''
    set -eux
    docker login -u "$DOCKERHUB_USER" -p "$DOCKERHUB_TOKEN"
	'''
}
