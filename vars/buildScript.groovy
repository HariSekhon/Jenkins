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
//                                   B u i l d
// ========================================================================== //

// Do not call this file build.groovy, it will clash with the built-in 'build' which is used by jenkinsfileLibraryUpdatePipeline.groovy

def call () {
  echo "Running Job '${env.JOB_NAME}' Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
  echo "Building from branch '${env.GIT_BRANCH}' for '" + "${env.ENVIRONMENT}".capitalize() + "' Environment"
  // XXX: prevents calling in a parallel stage otherwise you'll get this error:
  //
  //  "Using a milestone step inside parallel is not allowed"
  //
  milestone ordinal: null, label: "Milestone: Build"
  retry (2) {
    timeout (time: 40, unit: 'MINUTES') {
      // script in local repo
      sh 'build.sh'
    }
  }
}
