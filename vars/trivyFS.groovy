//
//  Author: Hari Sekhon
//  Date: 2023-05-12 17:45:24 +0100 (Fri, 12 May 2023)
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
//                  T r i v y   S c a n   L o c a l   F i l e s
// ========================================================================== //

// Security scanner

// Pass a starting directory to scan everything underneath it - defaults to '.' for $PWD which in CI/CD is your repo checkout

// For requirements see adjacent trivy.groovy
//
// Usage:
//
//      trivyFS()  // scan the git clone checkout in the root directory where we start the pipeline
//
//      trivyFS('src') // scan only the src code directory
//
//
//  Wrap in a 'catchError' to leave it as informational but not break the build - as it's very common for there to be some CVEs etc and you don't usually want it blocking people
//
//      catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//        trivyFS()
//      }
//
// XXX: set environment variable TRIVY_SERVER to use a Trivy server to not waste 15 minutes downloading the vulnerabilities DB on every Jenkins agent,
//      especially important if you're using auto-spawning agents on Kubernetes. On Kubernetes this should be set in Jenkins set this globally at $JENKINS_URL/configure to:
//
//        TRIVY_SERVER=http://trivy.trivy.svc.cluster.local:4954
//
//        https://github.com/HariSekhon/Kubernetes-configs/tree/master/trivy/base
//

def call (dir='.', severity='HIGH,CRITICAL', timeoutMinutes=20) {
  label 'Trivy'
  timeout (time: timeoutMinutes, unit: 'MINUTES') {
    withEnv (["DIR=$dir", "SEVERITY=$severity"]) {
      // Trivy won't show anything below --severity so need to run one without severity to get the full information
      echo "Trivy scanning dir '$DIR' - informational only to see all issues"
      trivy("fs '$DIR' --no-progress --timeout '${timeoutMinutes}m'")

      echo "Trivy generate report for dir '$DIR'"
      sh('mkdir -p reports')
      trivy("fs '$DIR' --no-progress --timeout '${timeoutMinutes}m' --format template --template '@trivy-html.tpl' -o 'reports/trivy-fs.html'")
      echo "Publish HTML report to Jenkins"
      publishHTML target : [
        allowMissing: true,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: 'reports',
        reportFiles: 'trivy-fs.html',
        reportName: 'Trivy FS Scan',
        reportTitles: 'Trivy FS Scan'
      ]

      echo "Trivy scanning dir '$DIR' for severity '$SEVERITY' vulnerabilities only - will fail if any are detected"
      trivy("fs '$DIR' --no-progress --timeout '${timeoutMinutes}m' --exit-code 1 --severity '$SEVERITY'")
    }
  }
}
