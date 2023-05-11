//
//  Author: Hari Sekhon
//  Date: 2022-07-19 15:59:50 +0100 (Tue, 19 Jul 2022)
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
//                            S l a c k   N o t i f y
// ========================================================================== //

// https://www.jenkins.io/doc/pipeline/steps/slack/

// Sends a Slack notification from any post{} section in a pipeline
//
// - auto-determines Git log committers between last Success and Broken builds
// - crafts a Slack specific message with:
//   - @here and Git log committers of breakage
//   - links to Job, Build Run
//   - whether to be green or red Slack notification depending on success of failure
//
// channel can be configured at the global Jenkins level when Slack integration is configured, or overridden via the first argument

def call (String channel='') {

  env.BUILD_RESULT = currentBuild.result ?: 'SUCCESS'

  //if (env.BUILD_RESULT == 'SUCCESS') {
  //if (['UNSTABLE', 'ABORTED'].contains(env.BUILD_RESULT)) {
  switch (env.BUILD_RESULT) {
    case 'SUCCESS':
      env.SLACK_COLOR = 'good'
      env.BUILD_RESULT = env.BUILD_RESULT.toLowerCase().capitalize()
      break
    // fallthrough to set warning for either of these conditions
    case 'UNSTABLE':
    case 'ABORTED':
      env.SLACK_COLOR = 'warning'
    default:
      env.SLACK_COLOR = 'danger'
  }

  env.SLACK_USERTAGS = ''
  if (env.SLACK_COLOR != 'good') {
    env.SLACK_USERTAGS = slackBrokenCommitters().join(' ')
  }
  if (env.SLACK_USERTAGS) {
    env.SLACK_USERTAGS = '- ' + env.SLACK_USERTAGS
  }

  // $BUILD_URL       for Classic UI
  // $RUN_DISPLAY_URL for Blue Ocean UI
  env.SLACK_LINKS  = "Pipeline <$JOB_DISPLAY_URL|$JOB_NAME> - <$RUN_DISPLAY_URL|Build #$BUILD_NUMBER>"
  env.SLACK_MESSAGE = "Job $BUILD_RESULT - $SLACK_LINKS $SLACK_USERTAGS"

  slackSend (
    // works but channels seem to work with or without # prefix, making this unnecessary
    //channel: "#${channel.replaceAll('^#+', '')}",
    channel: "$channel",
    color: "$SLACK_COLOR",
    message: "$SLACK_MESSAGE",
    notifyCommitters: true,
    // notifyCommitters requires bot user
    botUser: true,
    username: 'Jenkins'  // override display name
  )
}
