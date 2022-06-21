#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-06-21 13:12:02 +0100 (Tue, 21 Jun 2022)
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
//                     D o w n l o a d s   T e r r a f o r m
// ========================================================================== //

// Downloads Terraform binary to $HOME/bin, which should be added to $PATH

def call(version) {
  timeout(time: 3, unit: 'MINUTES') {
    installBinary(url: "https://releases.hashicorp.com/terraform/${version}/terraform_${version}_{os}_{arch}.zip", binary: 'terraform')
  }
}
