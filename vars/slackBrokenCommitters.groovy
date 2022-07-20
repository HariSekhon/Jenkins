//
//  Author: Hari Sekhon
//  Date: 2022-07-19 15:59:50 +0100 (Tue, 19 Jul 2022)
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
//                 S l a c k   B r o k e n   C o m m i t t e r s
// ========================================================================== //

// returns a List of Slack userIds from gitLogBrokenCommitters
//
// if a git log committer's email address is not resolved to a Slack userId, then returns the username instead

def call(){
    // this returns nothing - probably because Committers emails don't always match Slack emails
    //List userIds = slackUserIdsFromCommitters()
    //env.USERTAGS = userIds.collect { "<@$it>" }.join(' ')

    Map logCommitters = gitLogBrokenCommitters()
    List userTags
    logCommitters.each {
      def userId = slackUserIdFromEmail(it.value)
      if(userId){
        userTags.add("@$userId")
      } else {
        userTags.add(it.name)
      }
    }
    return userTags
}
