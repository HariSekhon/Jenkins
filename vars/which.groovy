#!/usr/bin/env groovy
//
//  Author: Hari Sekhon
//  Date: 2023-05-12 20:57:51 +0100 (Fri, 12 May 2023)
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
//                                   W h i c h
// ========================================================================== //

// Groovy equivalent to the unix 'which' command to return the path to an executable in the $PATH if found
//
// Behaves the same was as the traditional command and returns nothing (a blank string) if the file isn't found or if a file is not executable
//
// This has the dual use of either returning the path to the executable or just using it as a boolean test
//
// Usage:
//
//      if (! which('argocd')) {
//          downloadArgo()
//      }
//

def call(String executable) {
  String separator = File.pathSeparator

  for (dir in env.PATH.split(separators)) {
      def file = new File(dir, executable)
      if (file.canExecute()) {
          return file.absolutePath
      }
  }

  return ''
}
