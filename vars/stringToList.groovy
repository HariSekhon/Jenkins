//
//  Author: Hari Sekhon
//  Date: 2023-05-15 02:09:36 +0100 (Mon, 15 May 2023)
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
//                          S t r i n g   T o   L i s t
// ========================================================================== //

// utility function to allow other functions to accept either a single String or a List
// and pass it through this function to end up with a List of Strings either way
// account for

def call(arg) {
  List list = []
  if (arg) {
    if (arg instanceof String ||
        arg instanceof org.codehaus.groovy.runtime.GStringImpl) {
      list = [arg]
      //  ! arg instanceof List   does not work and
      //    arg !instanceof List  is only available in Groovy 3
    } else if (arg instanceof List == false) {
      error "non-list passed as first arg to stringToList() function"
    } else {
      list = arg
    }
  } else {
    error "no arg passed to stringToList() function"
  }
  return list
}
