//
//  Author: Hari Sekhon
//  Date: 2022-07-19 19:19:36 +0100 (Tue, 19 Jul 2022)
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
//                                  N o t i f y
// ========================================================================== //

// Wrapper to call all other notification functions to abstract any changes from the many calling pipelines

def call () {
  if (env.NO_NOTIFY != 'true') {
    // add all notification methods here:
    //
    // emailNotify()
    // ...
    //
    slackNotify()
  }
}
