//
//  Author: Hari Sekhon
//  Date: 2021-04-30 15:25:01 +0100 (Fri, 30 Apr 2021)
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
//             Find Git Log Committers to Notify they Broke the Build
// ========================================================================== //

// abstracted from previous pipelines gitMergePipeline, terraformPipe, jenkinsBackupJobConfigsPipeline

// Example Usage:
//
//      failure {
//        script {
//          env.LOG_COMMITTERS = gitLogBrokenCommitters()
//        }
//
//        // then use it in whichever notification method you want:
//
//        slackSend color: 'danger',
//          message: "Job FAILED - ${env.SLACK_MESSAGE} - @here ${env.LOG_COMMITTERS}",
//          botUser: true
//      }

def call() {
  logCommitters = sh (
    label: 'Get Git Log Committers Since Last Successful Build',
    returnStdout: true,
    script: '''
      set -eux
      if [ -z "${GIT_PREVIOUS_SUCCESSFUL_COMMIT:-}" ]; then
        exit 0
      fi
      git log --format='@%an' "${GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${GIT_COMMIT}" |
      grep -Fv -e Jenkins \
               -e '[bot]' |
      sort -fu |
      tr '\n' ' '
    '''
  ).trim()
  echo "Inferred Git committers since last successful build via git log to be: $logCommitters"
  return logCommitters
}
