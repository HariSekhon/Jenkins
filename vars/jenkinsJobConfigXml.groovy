//
//  Author: Hari Sekhon
//  Date: 2023-06-08 23:36:19 +0100 (Thu, 08 Jun 2023)
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
//                        J e n k i n s   J o b   X M L
// ========================================================================== //

// Returns a string of the Jenkins Job XML for a given job using the Jenkins CLI
//
// assumes JENKIN_CLI_JAR has been downloaded in a previous stage via downloadJenkinsCLI() function

def call(jobName) {
  withEnv(["JOB_NAME=$jobName"]) {
    String jobXml = sh (
      label: "Get Job XML via CLI",
      returnStdout: true,
      script: '''
        set -eux
        java -jar "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}" ${JENKINS_CLI_ARGS:-} get-job "$JOB_NAME"
      '''
    )
    if ( ! jobXml ) {
      error("Failed to retrieve Jenkins job config xml for job '$jobName'")
    }

    return jobXml
  }
}
