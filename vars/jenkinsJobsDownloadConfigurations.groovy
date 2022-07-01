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

def call(jobs=[]) {
  if(!jobs){
      jobs = sh (
        label: "List Jobs",
        returnStdout: true,
        script: '''
          set -eux
          java -jar ~/bin/jenkins-cli.jar ${JENKINS_CLI_ARGS:-} list-jobs
        '''
      ).tokenize('\n')
  }
  echo "Downloading configurations for ${jobs.size()} Jenkins jobs"
  jobs.eachWithIndex{ it, index ->
    withEnv(["JOB=$it"]){
      sh (
        // zero indexed
        label: "${index+1} Download Jenkins Job Configuration: $it",
        script: '''
          set -eux
          java -jar ~/bin/jenkins-cli.jar ${JENKINS_CLI_ARGS:-} get-job "$JOB" > "$JOB.xml"
          echo >> "$JOB.xml"
          echo "Downloaded config to file: $PWD/$JOB.xml"
        '''
      )
    }
  }
}
