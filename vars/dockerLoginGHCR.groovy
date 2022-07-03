//
//  Author: Hari Sekhon
//  Date: 2022-06-21 10:46:51 +0100 (Tue, 21 Jun 2022)
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
//                   Docker Login to GitHub Container Registry
// ========================================================================== //

// GITHUB_USER and GH_TOKEN / GITHUB_TOKEN must be passed as args or set in the calling environment

def call(user="$GITHUB_USER", token="${env.GH_TOKEN || env.GITHUB_TOKEN}") {
  dockerLogin(user, token, 'ghcr.io')
}
