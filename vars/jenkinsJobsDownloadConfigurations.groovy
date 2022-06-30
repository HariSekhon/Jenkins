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

def call(jobs=[]) {
  jobs.each{
    withEnv(["JOB=$it"]){
      sh (
        label: "Download Jenkins Job Configuration: $it",
        script: '''
          set -eux
          jar=''
          for path in ~/bin /usr/local/bin; do
            if [ -f "$path/jenkins-cli.jar" ]; then
              jar="$path/jenkins-cli.jar"
              break
            fi
          done
          if [ -z "$jar" ]; then
            echo 'jenkins-cli.jar not found!'
            exit 1
          fi
          java -jar "$jar" get-job "$JOB" > "$JOB.xml"
          echo >> "$JOB.xml"
          echo "Downloaded config to file: $PWD/{job}.xml"
        '''
      )
    }
  }
}
