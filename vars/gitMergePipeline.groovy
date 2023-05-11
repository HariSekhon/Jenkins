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
//              G i t   M e r g e   B r a n c h e   P i p e l i n e
// ========================================================================== //

// Usage in Jenkinsfile:
//
//    @Library('github.com/harisekhon/jenkins@master') _
//    gitMergePipeline('staging', 'dev')
//

def call (fromBranch, toBranch) {

  pipeline {

    agent any

    options {
      disableConcurrentBuilds()
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
    }

    stages {

      stage('Environment') {
        steps {
          printEnv()
        }
      }

      stage('Git Merge') {
        steps {
          gitMerge("$fromBranch", "$toBranch")
        }
      }

      // git push needs to be done in the same step gitMerge to benefit properly from the locking
      //stage('Git Push') {
      //  steps {
      //    sh (
      //      label: 'Git Push',
      //      script: 'git push origin --all'
      //    )
      //  }
      //}
    }

    post {
      failure {
        Notify()
      }
      fixed {
        Notify()
      }
    }

  }

}
