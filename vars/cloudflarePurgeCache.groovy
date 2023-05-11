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
//                  C l o u d f l a r e   P u r g e   C a c h e
// ========================================================================== //

// Requires environment variables to be defined in environment{} section of Jenkinsfile:
//
//    CLOUDFLARE_EMAIL
//    CLOUDFLARE_ZONE_ID
//    CLOUDFLARE_API_KEY  // see master ../Jenkinsfile for how to load this from a Jenkins secret

def call () {
  String label = "Cloudflare Purge Cache - '" + "${env.ENVIRONMENT}".capitalize() + "' Environment"
  echo "Acquiring Cloudflare Lock: $label"
  lock (resource: label, inversePrecedence: true) {
    milestone ordinal: null, label: "Milestone: $label"
    retry (2) {
      timeout (time: 1, unit: 'MINUTES') {
        echo "$label"
        sh (
          label: "$label",
          script: '''#!/usr/bin/env bash
            set -euxo pipefail
            output="$(
                curl -sS -X POST "https://api.cloudflare.com/client/v4/zones/$CLOUDFLARE_ZONE_ID/purge_cache" \
                     -H "X-Auth-Email: $CLOUDFLARE_EMAIL" \
                     -H "X-Auth-Key: $CLOUDFLARE_API_KEY" \
                     -H "Content-Type: application/json" \
                     --data '{"purge_everything":true}'
            )"
            #echo "$output"
            grep -q '"success": true' <<< "$output"
          '''
        )
      }
    }
  }
}
