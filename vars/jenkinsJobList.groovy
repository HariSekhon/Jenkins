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
//                        J e n k i n s   J o b   L i s t
// ========================================================================== //

// Returns a list of strings of jenkins job names
//
// assumes JENKIN_CLI_JAR has been downloaded in a previous stage via downloadJenkinsCLI() function

def call() {
  // requires several iterations of In-process Script Approvals from repeatedly failing pipelines at each level of descent into the jenkins.model hierarchy
  //List<String> jobs = jenkins.model.Jenkins.instance.items.findAll().collect { it.name }
  // might be able to be more concisely written as a spread expression:
  //                    jenkins.model.Jenkins.instance.items.findAll()*.name

  List<String> jobs = sh (
    label: "List Jobs via CLI",
    returnStdout: true,
    script: '''
      set -eux
      java -jar "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}" ${JENKINS_CLI_ARGS:-} list-jobs
    '''
  ).tokenize('\n')

  return jobs
}
