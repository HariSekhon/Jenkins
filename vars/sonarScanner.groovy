//
//  Author: Hari Sekhon
//  Date: 2022-01-06 17:19:11 +0000 (Thu, 06 Jan 2022)
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
//                         S o n a r   S c a n n e r
// ========================================================================== //

// https://docs.sonarqube.org/latest/analyzing-source-code/scanners/jenkins-extension-sonarqube/
//
// Requires Jenkins Sonar plugin:
//
//      https://plugins.jenkins.io/sonar/

// CLI Scanner for SonarQube
//
// Repo should have a sonar-project.properties at its root for the project specific settings
//
// Jenkins should have its Global Config for the SonarQube section set at:
//
//    $JENKINS_URL/configure
//
// to provide pipeline with:
//
//    SONAR_TOKEN credential - https://sonar.domain.com/account/security
//
//    SONAR_HOST_URL = https://sonar.domain.com (via Kubernetes ingress, see https://github.com/HariSekhon/Kubernetes-configs
//

// 'config' is the server config instance configured at $JENKINS_URL/configure "SonarQube servers" section
//
// 'toolName' is the global tool name configured with version at $JENKINS_URL/configureTools/ "SonarQube Scanner" section
//
def call (config='SonarQube Server on Kubernetes', toolName='SonarQube Scanner', timeoutMinutes=30) {
  label 'Sonar Scanner'
  // let caller decide if wrapping this in a container('grype') or using downloadGrype.groovy to save RAM
  //container('sonar-scanner') {
    timeout (time: timeoutMinutes, unit: 'MINUTES') {
      ansiColor('xterm') {
        withSonarQubeEnv(config) {
          def scannerHome = tool toolName
          echo "Sonar Scanner using SonarQube server at '$SONAR_HOST_URL'"
          sh (
            label: "Sonar Scanner",
            script: "${scannerHome}/bin/sonar-scanner"
          )
        }
      }
    }
  //}
}
