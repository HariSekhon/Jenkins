//
//  Author: Hari Sekhon
//  Date: 2022-06-30 17:52:05 +0100 (Thu, 30 Jun 2022)
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
//                       Delete Removed Jenkins Job Configs
// ========================================================================== //

// Deletes any XML files in the current directory that do not match Jenkins Jobs found on the server
//
// dir() should be called before this function (see adjacent jenkinsBackupJobConfigsPipeline.groovy)
//
// This is to clean up Git commits to remove old jobs that have been renamed or deleted

def call () {

  Boolean jenkinsCliJarExists = sh (
    label: 'Check Jenkins CLI jar exists',
    returnStatus: true,
    script: 'test -f "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}"'
  ) == 0

  if (!jenkinsCliJarExists) {
    echo "$jenkinsCliJar not found, downloading..."
    downloadJenkinsCLI()
  }

  List<String> jobList = sh (
    label: "List Jobs via CLI",
    returnStdout: true,
    script: '''
      set -eux
      java -jar "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}" ${JENKINS_CLI_ARGS:-} list-jobs
    '''
  ).tokenize('\n')

  // DIR() {} containing section only seems to apply to shell and not the JVM runtime which gets / directory instead of the one we asked for
  String pwd = sh (
    returnStdout: true,
    script: '''
      set -eux
      pwd
    '''
  )
  echo "Getting list of XML files in current directory: ${pwd}"
  // not handling recursion because Jenkins jobs are backed up to a single directory, not subdirectories
  // see adjacent jenkinsJobsDownloadConfigurations.groovy
  ////def baseDir = new File('.');  // gets / directory of the runtime instead of DIR() {} from Jenkins
  //def baseDir = new File(pwd);
  // XXX: for some reason this returns no files even though File(pwd) is set to the real current shell directory full of *.xml files that ls and find return
  //def fileList = baseDir.listFiles()

  //echo "filtering XML files:"
  //// return basenames of only xml files
  //List<String> xmlFileList = fileList.collect { filename ->
  //  if ( filename.name.endsWith('.xml') ) {
  //    filename.name.split('/')[-1]
  //  }
  //}

  List xmlFileList = sh (
    returnStdout: true,
    script: '''
      set -eux
      find . -maxdepth 1 -name '*.xml'
    '''
  ).tokenize('\n')

  if (!xmlFileList) {
    error "No XML files found in current directory: ${baseDir.canonicalPath}"
  }

  echo "Found ${xmlFileList.size()} XML files in current directory"

  echo "Deleting any XML files which don't have a corresponding current Jenkins Job"
  xmlFileList.each { filename ->
    withEnv(["FILENAME=$filename"]) {
      echo "Checking file '$filename' for corresponding Jenkins job"
      String fileBaseName = filename.split('/')[-1]
      String jobName = fileBaseName.split('\\.xml$')[0]
      if ( ! jobList.contains(jobName) ) {
        echo "Jenkins job '$jobName' not found"
        sh (
          label: "Deleting $filename",
          script: '''
            set -eux
            rm -fv "$PWD/$FILENAME"
          '''
        )
      }
    }
  }
}
