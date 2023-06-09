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
//                        J e n k i n s   J o b   L i s t
// ========================================================================== //

// Returns a list of strings of jenkins job names - lighter weight than using the Jenkins CLI but requires in-process script approval by an Administrator

def call() {
  // requires several iterations of In-process Script Approvals from repeatedly failing pipelines at each level of descent into the jenkins.model hierarchy
  //List<String> jobs = jenkins.model.Jenkins.instance.items.findAll().collect { it.name }
  List<String> jobs = jenkins.model.Jenkins.instance.items.findAll()*.name

  return jobs
}
