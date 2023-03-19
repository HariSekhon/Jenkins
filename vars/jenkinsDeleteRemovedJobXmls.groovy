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
//                      Download Jenkins Job Configurations
// ========================================================================== //

// Deletes any XML files in the current directory that do not match Jenkins Jobs found on the server
//
// dir() should be called before this function (see adjacent jenkinsBackupJobConfigsPipeline.groovy)
//
// This is to clean up Git commits to remove old jobs that have been renamed or deleted

def call() {

  Boolean jenkinsCliJarExists = sh(
    label: 'Check Jenkins CLI jar exists',
    returnStatus: true,
    script: 'test -f "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}"'
  ) == 0

  if(!jenkinsCliJarExists){
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

  def baseDir = new File('.');
  // not handling recursion because Jenkins jobs are backed up to a single directory, not subdirectories
  // see adjacent jenkinsJobsDownloadConfigurations.groovy
  def fileList = baseDir.listFiles()

  // return basenames of only xml files
  List<String> xmlFileList = fileList.each { filename ->
    if( filename.toString().endsWith('.xml') ){
      filename.name.split('/')[-1]
    }
  }

  echo "Deleting any XML files which don't have a corresponding current Jenkins Job"
  xmlFileList.each { filename ->
    String jobName = filename.split('\\.xml$')[0]
    if ( ! jobList.contains(jobName) ){
      sh (
        label: "Deleting $fileBaseName",
        script: """
          set -eux
          rm -fv "$filename"
        """
      )
    }
  }
}
