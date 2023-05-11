//
//  Author: Hari Sekhon
//  Date: 2022-01-19 17:54:02 +0000 (Wed, 19 Jan 2022)
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
//                                 C h e c k o v
// ========================================================================== //

def call (timeoutMinutes=10) {
  container('checkov') {
    timeout (time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        sh label: 'Checkov',
           script: 'checkov -d . -o junitxml > result.xml || true'
        junit 'result.xml'
      }
    }
  }
}
