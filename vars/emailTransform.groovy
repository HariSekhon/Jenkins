//
//  Author: Hari Sekhon
//  Date: 2022-07-20 12:44:11 +0100 (Wed, 20 Jul 2022)
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
//                         E m a i l   T r a n s f o r m
// ========================================================================== //

// Munges an email from one format to another based on rules available in the environment for maximum portability
//
// Rules:
//
//  1. If EMAIL_TRANSFORMS environment variable is set, for each comma separated 'key=value' pair, replaces the key with the value in the email address via regex match. Powerful, use carefully
//  2. If EMAIL_DOMAIN_TRANSFORM environment variable is set, replaces anything after the @ symbol with that domain
//     - XXX: warning - this can lead to collisions if 2 different people have the same username prefix portions of email addresses from different domains eg. john@domain1.com and john@domain2.com both get munged to john@$EMAIL_DOMAIN

def call (String email) {
  String originalEmail = email
  // define matcher locally so @NonCPS excludes from serialization
  def matcher
  if (env.EMAIL_TRANSFORMS) {
    // would have preferred newline split but Jenkins global env var doesn't allow newlines, even pasted multiline becomes join(' ') in UI,
    // and not DRY to define in pipelines where we can use actual multiline strings
    List emailTransformList = env.EMAIL_TRANSFORMS.trim().split(',').collect { it.trim() }
    Map emailTransforms = emailTransformList.collectEntries {
      // ==~ anchored match returns boolean
      // requires brackets around ==~ otherwise ! test always returns negative
      if ( ! it || ! ( it ==~ /^[^=]+=[^=]+$/ ) ) {
        echo ("WARNING: invalid EMAIL_TRANSFORMS entry: $it")
        return [:]
      }
      String key = it.split('=')[0].trim()
      String value = it.split('=')[-1].trim()
      // using bare key overwrites all values into a single literal 'key' item
      return [ (key.toString()) : value ]
    }
    emailTransforms.each {
      email = email.replaceFirst(/$it.key/, "$it.value")
    }
  }
  if (env.EMAIL_DOMAIN_TRANSFORM) {
    String newDomain = env.EMAIL_DOMAIN_TRANSFORM.trim()
    if (newDomain) {
      email = email.replaceFirst(/@.+$/, "@$newDomain")
    }
  }
  if (email != originalEmail) {
    echo "Transformed email from '$originalEmail' to '$email'"
  }
  return email
}
