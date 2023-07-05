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
//                                A p p r o v a l
// ========================================================================== //

// Prompts for human click approval before progressing - protect your production environments from deployments

// Usage:
//
//             // submitter can be in group email or name format
//    approval(submitter: 'platform-engineering@mycompany.com,ApproversGroup', timeout: 10)
//
// or better to DRY between pipelines use a global variable:
//
//    approval(submitter: "$APPROVERS", timeout: 10)
//
// set to 2 hours instead of default 60 minutes - these values are those supported by the standard Jenkins timeout() function:
//
//    approval(submitter: "$APPROVERS", timeout: 2, timeoutUnits: 'HOURS')
//
// then configure $APPROVERS environment variable at the global Jenkins level:
//
//    Manage Jenkins -> Configure System -> Global properties -> Environment Variables -> Add -> APPROVERS
//
// submitter = comma separated list of users/groups by name or email address that are permitted to authorize
// ok        = what the ok button should say, defaults to 'Proceed' if empty/unspecified

def call (Map args = [submitter:'', timeout:60, timeoutUnits: 'MINUTES', ok:'']) {
  // XXX: prevents calling in a parallel stage otherwise you'll get this error:
  //
  //  "Using a milestone step inside parallel is not allowed"
  //
  milestone ordinal: null, label: "Milestone: Approval"
  int time = args.timeout ?: 60
  String timeoutUnits = args.timeoutUnits ?: 'MINUTES'
  timeout (time: time, unit: timeoutUnits) {
    input (
      message: """Are you sure you want to release this build?

This prompt will time out in ${time} ${timeoutUnits.toLowerCase()}""",
      ok: args.ok,
      // only allow people in these 2 groups to approve before proceeeding eg. to production deployment - this list can now be provided as an argument
      //submitter: "platform-engineering@mydomain.co.uk,ApproversGroup"
      submitter: args.submitter
    )
  }
}
