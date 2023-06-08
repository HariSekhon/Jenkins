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

// inspired by jenkins_jobs_download_configs_cli.sh in the adjacent DevOps Bash tools repo
//
// assumes JENKIN_CLI_JAR has been downloaded in a previous stage via downloadJenkinsCLI() function

// this function
//
//  takes 2 mins 22 secs to download 49 Jenkins jobs
//
// whereas jenkins_jobs_download_configs.sh from my DevOps Bash tools repo
//
//  takes 0 mins 22 secs to download same 49 job configs (8 secs user, 6 secs sys)
//
// nearly half of this is accounted for in JVM startup overheads of jenkins-cli.jar since jenkins_jobs_download_configs_cli.sh
//
//  takes 0 mins 52 secs for the same as above (1m08secs user, 7secs sys - meaning the JVM is > 1 CPU intensive compared to Rest API calls which are minor CPU)
//
// the rest of the speed loss must be Jenkins withEnv + new shell overheads of 'sh' x49

def call (Map args = [ jobs: [], excludeJobs: [] ]) {

  // avoiding using regex due to non-serialization and need to use @NonCPS annotation which breaks groovy checks, which then have to be disabled, leaving the whole function unvalidated
  List defaultExcludedJobs = [
    'test',
    'Test'
  ]

  List<String> jobs = args.jobs ?: []
  List<String> excludedJobs = args.excludeJobs ?: defaultExcludedJobs

  //jenkinsCliJar = env.JENKINS_CLI_JAR ?: "$HOME/bin/jenkins-cli.jar"
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

  if (!jobs) {
      jobs = jenkinsJobList()
  }
  int numJobs = jobs.size()

  // org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use staticMethod jenkins.model.Jenkins getInstance
  //echo "List jobs via Jenkins API"
  //List jobs2 = jenkins.model.Jenkins.instance.items.findAll().collect { it.name }
  //int numJobs2 = jobs2.size()

  //echo "jobs from CLI = $jobs"
  //echo "jobs from API = $jobs2"
  //echo "number of jobs from CLI = $numJobs"
  //echo "number of jobs from API = $numJobs2"
  //if (jobs != jobs2) {
  //  error("ERROR: job lists don't watch between CLI and API results")
  //}
  //assert numJobs == numJobs2

  jobs -= excludedJobs
  int numExcludedJobs = numJobs - jobs.size()
  numJobs = jobs.size()
  if (numExcludedJobs != 0) {
    echo "$numExcludedJobs jobs excluded"
  }

  echo "Downloading configurations for $numJobs Jenkins jobs"
  jobs.eachWithIndex { it, index ->
    withEnv (["JOB=$it"]) {
      sh (
        // zero indexed
        label: "${ index + 1 } Download Jenkins Job Configuration: $it",
        script: '''
          set -eux
          java -jar "${JENKINS_CLI_JAR:-$HOME/bin/jenkins-cli.jar}" ${JENKINS_CLI_ARGS:-} get-job "$JOB" > "$JOB.xml"
          echo >> "$JOB.xml"
          if command -v xmllint >/dev/null 2>&1; then
            tmp="$(mktemp)"

            # --nowarning because we don't want this cluttering the logs and having people raising questions:
            #
            # $JOB.xml:1: parser warning : Unsupported version '1.1'
            # <?xml version='1.1' encoding='UTF-8'?>
            #
            xmllint --nowarning "$JOB.xml" > "$tmp"

            mv "$tmp" "$JOB.xml"
          fi
          echo "Downloaded config to file: $PWD/$JOB.xml"
        '''
      )
    }
  }
}
