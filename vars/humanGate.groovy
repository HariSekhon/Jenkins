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

// Usage:
//
//    humanGate(submitter: 'platform-engineering@mycompany.com,Deployers', timeout: 10)
//
// submitter = comma separated list of users/groups by name or email address that are permitted to authorize

def call(Map args = [submitter:'', timeoutMinutes:60]){
  milestone ordinal: 20, label: "Milestone: Human Gate"
  timeout(time: args.timeoutMinutes, unit: 'MINUTES') {
    input (
      message: """Are you sure you want to release this build?

This prompt will time out""",
      ok: "Deploy",
      // only allow people in these 2 groups to approve this human gate before deployments, useful for production - this list can now be provided as an argument
      //submitter: "platform-engineering@mydomain.co.uk,Deployers"
      submitter: args.submitter
    )
  }
}
