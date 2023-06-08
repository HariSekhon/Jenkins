//
//  Author: Hari Sekhon
//  Date: 2022-06-30 18:03:25 +0100 (Thu, 30 Jun 2022)
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
//                Download Jenkins CLI from current Jenkins Master
// ========================================================================== //

// Downloads Terraform binary to $HOME/bin if run as a user, or /usr/local/bin if run as root

// Adapted from DevOps Bash Tools jenkins_cli.sh, install_binary.sh, install_packages.sh and lib/utils.sh

def call () {

  // groovy.lang.MissingPropertyException: No such property: JENKINS_CLI_JAR for class: groovy.lang.Binding
  //jenkinsCliJar = env.JENKINS_CLI_JAR ?: "$HOME/bin/jenkins-cli.jar"
  //
  //if (env.JENKINS_CLI_JAR) {
  //  jenkinsCliJar = env.JENKINS_CLI_JAR
  //} else {
  //  jenkinsCliJar = "$HOME/bin/jenkins-cli.jar"
  //}
  //
  // org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use new java.io.File java.lang.String
  //jenkinsCliJarFile = new File(jenkinsCliJar)
  //if (! (new File(jenkinsCliJar)).exists() ) {

  //Boolean jenkinsCliJarExists = sh (
  //  label: 'Check Jenkins CLI jar exists',
  //  returnStatus: true,
  //  script: 'test -f "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}"'
  //) == 0
  //
  //if (!jenkinsCliJarExists) {
  //  echo "$jenkinsCliJar not found, downloading..."
  //  downloadJenkinsCLI()
  //}

  String label = "Download Jenkins CLI on agent '$HOSTNAME'"
  echo "Acquiring Lock: $label"
  lock (resource: "$label") {
    timeout (time: 5, unit: 'MINUTES') {
      // TODO: add destination arg to follow ${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}
      installBinary(url: "$JENKINS_URL/jnlpJars/jenkins-cli.jar")
      sh (
        label: "Jenkins CLI Version",
        // clear JENKINS_URL and JENKINS_USER_ID to avoid this error:
        //  "The JENKINS_USER_ID and JENKINS_API_TOKEN env vars should be both set or left empty."
        script: '''
          set -eu
          #unset JENKINS_URL JENKINS_SERVER_COOKIE JENKINS_USER_ID JENKINS_NODE_COOKIE
          unset $(env | grep '^JENKINS_' | sed 's/=.*//')
          java -jar ~/bin/jenkins-cli.jar version || :
        '''
      )
    }
  }
}
