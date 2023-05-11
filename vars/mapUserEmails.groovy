//
//  Author: Hari Sekhon
//  Date: 2021-04-30 15:25:01 +0100 (Fri, 30 Apr 2021)
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
//                          M a p   U s e r   E m a i l
// ========================================================================== //

// Processes a List of ['Username <email>']
//        to a Map  of [username:email]
//
// see gitLogBrokenCommitters.groovy for example usage

// Limitation: usernames that have used more than one email addresses will overwrite with the last email in the list

// XXX: matcher is non-serializable and will result in this exception in Jenkins due to it wanting to be able to save state to disk for durability/resume:
//
//  Caused: java.io.NotSerializableException: java.util.regex.Matcher
//
// the @NonCPS annotation tells Jenkins not to try to save the local variables of this function
// XXX: side effect, seems to generate 'at Unknown.Unknown(Unknown)' for any issue in Java stack traces in the calling function eg. slackBrokenCommitters()

// https://www.jenkins.io/doc/book/pipeline/cps-method-mismatches/
@NonCPS
def call (List userEmailList) {
  // returns a Map in ['user': 'email'] format
  Map userEmails = [:]
  // matcher needs local def, otherwise @NonCPS annotation doesn't exclude it
  def matcher
  userEmailList.each {
    if ((matcher = it =~ /^(.+)<(.+)>$/)) {
      username = matcher.group(1).trim()
      email = matcher.group(2).trim()
      username = username ?: email
      userEmails.put(username, email)
    } else {
      if (it) {
        echo("WARNING: failed to parse username<email>: $it")
      }
    }
  }
  return userEmails
}
