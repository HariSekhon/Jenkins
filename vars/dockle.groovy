//
//  Author: Hari Sekhon
//  Date: 2023-05-16 02:46:54 +0100 (Tue, 16 May 2023)
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
//                                  D o c k l e
// ========================================================================== //

// Dockle container security scanner

// Usage:
//
//  If you're running on a Jenkins agent that already has the dockle binary bundled just call it otherwise download dockle first
//  Downloading Dockle only takes 7 seconds in testing
//
//      downloadDockle()
//
//      dockle('image:tag')
//
//  If you want to make it informational but not break the build:
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        dockle('...')
//      }
//
//
// Set environment variables to access private registries
//
//    https://github.com/goodwithtech/dockle#authorization-for-private-docker-registry

def call (args='') {
  label 'Dockle'
  // let caller decide if wrapping this in a container('dockle') or using downloadDockle.groovy to save RAM
  //container('dockle') {
    ansiColor('xterm') {
      sh (
        label: "Dockle",
        script: "dockle --exit-code 1 $args"
      )
    }
  //}
}
