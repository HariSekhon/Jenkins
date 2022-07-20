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
// if a git log committer's email address is not resolved to a Slack userId, then attempts to use emailTransform() rules and tries using that transformed email address
// if Slack still cannot resolve the email address, then returns the username instead

def call(){
  // this returns nothing - probably because Committers emails don't always match Slack emails - so doing more advanced handling below with emailTranform() fallback
  //List userIds = slackUserIdsFromCommitters()
  //String userTags = userIds.collect { "<@$it>" }

  Map logCommitters = gitLogBrokenCommitters()
  List userTags = []
  logCommitters.each {
    String email = it.value
    // echo's the username that is being tried
    String userId = slackUserIdFromEmail(email)
    if(!userId){
      //echo "Slack ID not found for email '$email'"
      String originalEmail = email
      email = emailTransform(email)
      if(email != originalEmail){
        // slackUserIdFromEmail() echo's the username that is being tried, so this is not needed
        //echo "Trying to resolve Slack user using transformed email '$email'"
        userId = slackUserIdFromEmail(email)
      }
    }
    if(userId){
      userTags.add("<@$userId>")
    } else {
      String name = it.key
      echo "Email '$email' didn't resolve to any Slack user, using name '$name' instead"
      // the @ doesn't work, but makes firstname/lastname groupings across whitespace a little more obvious
      userTags.add("@$name")
    }
  }
  return userTags
}
