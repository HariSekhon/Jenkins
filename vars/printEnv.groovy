//
//  Author: Hari Sekhon
//  Date: 2021-08-31 17:53:01 +0100 (Tue, 31 Aug 2021)
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
//             P r i n t   E n v i r o n m e n t   V a r i a b l e s
// ========================================================================== //

// Call this at the start of every pipeline to make debugging easier by having this info always available

def call () {
  timeout (time: 1, unit: 'MINUTES') {
    String label = 'Environment'
    sh (
      label: "$label",
      script: '''
        set -eux
        env | sort
      '''
    )
  }
}
