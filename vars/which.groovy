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
  // not allowed in Groovy sandbox:
  // org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use staticField java.io.File pathSeparator
  //String separator = File.pathSeparator
  // groovy.lang.MissingPropertyException: No such property: FileSystems for class: groovy.lang.Binding
  //String separator = FileSystems.default.getSeparator()

  // both methods blocked, let's try this another way...
  //for (dir in env.PATH.split('/')) {
    // not allowed in Groovy sandbox
    //def file = new File(dir, executable)
    //if (file.canExecute()) {
    //  return file.absolutePath
    //
    //def file = FileSystems.default.getPath(dir, executable)
    //if (file.isExecutable()) {
    //  return file.toAbsolutePath().toString()
    //}
  //}
  //return ''
  withEnv(["EXECUTABLE=$executable"]) {
    def path = sh(
      label: "Which $EXECUTABLE",
      //returnStatus: true, // overrides returnStdout
      returnStdout: true,
      // try all 3 because some agents containers might not have the which binary installed or bash for the type -P command
      // not only is type -P not available, but it outputs this in the output annoyingly so use without -P even though it might accidentally catch non-binaries
      // type -P $EXECUTABLE 2>/dev/null ||
      // for some reason 'type' command is printing not found to stdout, ruining the expected behaviour
      // type $EXECUTABLE 2>/dev/null ||
      script: """
        which $EXECUTABLE 2>/dev/null ||
        command -v $EXECUTABLE 2>/dev/null || :
      """
    )
    return path
  }
}
