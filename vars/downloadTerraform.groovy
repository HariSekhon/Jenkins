#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2022-06-21 13:12:02 +0100 (Tue, 21 Jun 2022)
//
//  vim:ts=4:sts=4:sw=4:et
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


def call(version) {
    withEnv(["TERRAFORM_VERSION=$version"]){
        installBinary(url: "https://releases.hashicorp.com/terraform/v$TERRAFORM_VERSION/terraform_v${TERRAFORM_VERSION}_{os}_{arch}.zip", binary: 'terraform')
    }
}
