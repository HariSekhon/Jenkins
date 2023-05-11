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
//                   Docker Login to GitLab Container Registry
// ========================================================================== //

// GITLAB_USER and GITLAB_TOKEN must be passed as args or set in the calling environment

def call (user='', token='') {
  user = user ?: env.GITLAB_USER ?: error('dockerLoginGitlab: user not specified and GITLAB_USER not set in the environment')
  token = token ?: env.GITLAB_TOKEN ?: error('dockerLoginGitlab: token not specified and GITLAB_TOKEN not set in the environment')
  dockerLogin(user, token, 'registry.gitlab.com')
}
