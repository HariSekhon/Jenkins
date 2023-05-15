//
//  Author: Hari Sekhon
//  Date: 2023-05-12 17:45:24 +0100 (Fri, 12 May 2023)
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
//                  G r y p e   S c a n   L o c a l   F i l e s
// ========================================================================== //

// Security scanner

// Pass a starting directory to scan everything underneath it - defaults to '.' for $PWD which in CI/CD is your repo checkout

// For requirements see adjacent grype.groovy
//
// Usage:
//
//      grypeFs()  // scan the git clone checkout in the root directory where we start the pipeline
//
//      grypeFs('src') // scan only the src code directory
//
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        grypeFS()
//      }
//

def call (dir='.', failOn='high', timeoutMinutes=10) {
  label 'Grype'
  timeout (time: timeoutMinutes, unit: 'MINUTES') {
    withEnv (["DIR=$dir"]) {
      grype(["dir:$DIR"], failOn, timeoutMinutes)
    }
  }
}
