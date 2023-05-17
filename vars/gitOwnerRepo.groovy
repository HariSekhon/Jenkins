//
//  Author: Hari Sekhon
//  Date: 2023-02-17 19:19:21 +0000 (Fri, 17 Feb 2023)
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
//                 G i t   < O w n e r > / < R e p o >   N a m e
// ========================================================================== //

// Returns the name of the current Git repo in the <owner>/<repo> format by inferring from it the GIT_URL environment variable

def call () {
  String repoUrl = env.GIT_URL

  // strip any git@github.com: prefix
  String ownerRepo = repoUrl.split(':')[-1]

  // take the last two components from "https://github.com/<owner>/<repo>" to leave just "<owner>/<repo>"
  ownerRepo = ownerRepo.split('/')[-2, -1].join('/')

  return ownerRepo
}
