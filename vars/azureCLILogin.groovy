#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-07-03 19:11:10 +0100 (Sun, 03 Jul 2022)
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
//                      L o g i n   t o   A z u r e   C L I
// ========================================================================== //

def call(user, pass) {
  withEnv(["AZURE_USER=$user", "AZURE_PASSWORD=$pass"]){
    sh (
      label: 'Azure CLI Login',
      script: 'az login -u "$AZURE_USER" -p "$AZURE_PASSWORD"'
    )
    sh (
      label: 'Azure CLI Show Signed In User',
      script: 'az ad signed-in-user show'
    )
  }
}
