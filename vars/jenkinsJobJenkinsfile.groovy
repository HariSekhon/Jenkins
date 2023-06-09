//
//  Author: Hari Sekhon
//  Date: 2023-06-08 23:36:19 +0100 (Thu, 08 Jun 2023)
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
//                 J e n k i n s   J o b   J e n k i n s f i l e
// ========================================================================== //

// Returns the Jenkinsfile path for a given Jenkins job from its XML config as returned by jenkinsJobConfigXml(jobName)

def call(jobXml) {

  if ( ! jobXml ) {
    error('no job xml passed to function jenkinsJobJenkinsfile()')
  }

  // https://groovy-lang.org/processing-xml.html

  //def xmlroot = new XmlSlurper().parseText(jobXml)
  // gets java.lang.NullPointerException
  //assert xmlroot instanceof groovy.xml.slurpersupport.GPathResult

  //def xmlroot = new XmlParser().parseText(jobXml)

  //assert xmlroot instanceof groovy.util.Node

  // works in groovysh but not in Jenkins:
  //
  //    org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: No such field found: field groovy.util.Node **
  //
  //repo = xmlroot.'**'.find { it.name() == 'scriptPath' }.value()[0].trim()

  // quick and dirty
  repo = jobXml.
            split('\n').
            find { it.contains('<scriptPath>') }.
            replace('<scriptPath>', '').
            replace('</scriptPath>', '').
            trim()

  return repo
}
