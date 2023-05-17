//
//  Author: Hari Sekhon
//  Date: 2023-05-15 02:08:29 +0100 (Mon, 15 May 2023)
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
//                             D o c k e r   I n f o
// ========================================================================== //

def call() {
  label 'Docker Info'
  timeout (time: 2, unit: 'MINUTES') {
    echo "Docker Info"
    sh(
      label: 'Docker Info',
      script: 'docker info'
    )
  }
}
