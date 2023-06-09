//
//  Author: Hari Sekhon
//  Date: 2023-06-09 04:30:50 +0100 (Fri, 09 Jun 2023)
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
//                      J e n k i n s   J o b   B r a n c h
// ========================================================================== //

// Returns the branch for a given Jenkins job from its XML config as returned by jenkinsJobConfigXml(jobName)

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
  //branch = xmlroot.'**'.find { it.name() == 'name' }.value()[0].split('/')[-1].trim()

  // quick and dirty
  repo = jobXml.
            split('\n').
            find { it.contains('<name>') }.
            replace('<name>', '').
            replace('</name>', '').
            split('/')[-1].
            trim()

  return repo
}
