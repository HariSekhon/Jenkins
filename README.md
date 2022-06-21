# Jenkinsfile & Jenkins Shared Library

[![GitHub stars](https://img.shields.io/github/stars/HariSekhon/Jenkins?logo=github)](https://github.com/HariSekhon/Jenkins/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/HariSekhon/Jenkins?logo=github)](https://github.com/HariSekhon/Jenkins/network)
[![Lines of Code](https://img.shields.io/badge/lines%20of%20code-4.0k-lightgrey?logo=codecademy)](https://github.com/HariSekhon/Jenkins)
[![License](https://img.shields.io/github/license/HariSekhon/Jenkins)](https://github.com/HariSekhon/Jenkins/blob/master/LICENSE)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/HariSekhon/Jenkins?logo=github)](https://github.com/HariSekhon/Jenkins/commits/master)

[![CI Builds Overview](https://img.shields.io/badge/CI%20Builds-Overview%20Page-blue?logo=circleci)](https://harisekhon.github.io/CI-CD/)
[![Jenkinsfile](https://github.com/HariSekhon/Jenkins/actions/workflows/jenkinsfile.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/jenkinsfile.yaml)
[![Groovy](https://github.com/HariSekhon/Jenkins/actions/workflows/groovyc.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/groovyc.yaml)
[![YAML](https://github.com/HariSekhon/Jenkins/actions/workflows/yaml.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/yaml.yaml)
[![Validation](https://github.com/HariSekhon/Jenkins/actions/workflows/validate.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/validate.yaml)
[![Semgrep](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep.yaml)
[![Semgrep Cloud](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep-cloud.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep-cloud.yaml)
[![Kics](https://github.com/HariSekhon/Jenkins/actions/workflows/kics.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/kics.yaml)

[![Repo on Azure DevOps](https://img.shields.io/badge/repo-Azure%20DevOps-0078D7?logo=azure%20devops)](https://dev.azure.com/harisekhon/GitHub/_git/Jenkins)
[![Repo on GitHub](https://img.shields.io/badge/repo-GitHub-2088FF?logo=github)](https://github.com/HariSekhon/Jenkins)
[![Repo on GitLab](https://img.shields.io/badge/repo-GitLab-FCA121?logo=gitlab)](https://gitlab.com/HariSekhon/Jenkins)
[![Repo on BitBucket](https://img.shields.io/badge/repo-BitBucket-0052CC?logo=bitbucket)](https://bitbucket.org/HariSekhon/Jenkins)

Advanced Jenkinsfile & Jenkins Shared Library.

- [Jenkinsfile](https://github.com/HariSekhon/Jenkins/blob/master/Jenkinsfile) - Jenkins Pipeline master config
- [vars/](https://github.com/HariSekhon/Jenkins/tree/master/vars) - Jenkins Shared Library functions

Spliced from [HariSekhon/Templates](https://github.com/HariSekhon/Templates), for which this is now a submodule.

## QuickStart

Jenkinsfile:
```groovy
// load this library straight from github - the '_' at the end imports all functions
@Library('github.com/harisekhon/jenkins@master') _

pipeline {
  stages {
    stage('My Example'){
      steps {
        // call any function from this libary by its filename under vars/... without the .groovy extension
        //
        // see each var/<function>.groovy file for any arguments
        //
        // calls vars/printEnv.groovy
        printEnv()

        // log in to GCP cloud with a service account key
        gcpActivateServiceAccount()

        // log in to DockerHub
        dockerLogin()

        // log in to AWS ECR
        dockerLoginECR()

        // log in to AWS GCR
        dockerLoginGCR()

        // show all your authentications and who you're logged in as
        printAuth()

        // launch a GCP Cloud Build job, by default against your cloudbuild.yaml if no args given
        gcpCloudBuild()

        // run a script with locks to prevent another script or deployment from happening at the same time, timeout after 30 mins
        // newer runs will wait to acquire the locks, older intermediate runs will be skipped
        scriptLockExecute('/path/to/script.sh', ['deployment lock', 'script lock'], 30)

        // download, extract and install a specific version of a binary to /usr/local/bin if root or $HOME/bin if run as a user
        // here ${version} is a variable previously defined, while {os} and {arch} with no dollar sign are auto-inferred placeholders
        installBinary(url: "https://releases.hashicorp.com/terraform/${version}/terraform_${version}_{os}_{arch}.zip", binary: 'terraform')

        // require human approval before proceeding to production deployment
        humanGate()

        // deploy to Kubernetes via ArgoCD
        argoDeploy()

        // see under vars/ for many more useful functions
      }
    }
  }
}
```

## Terraform

Run the `terraformPipeline` for a reusable parameterized Terraform CI/CD -  handles all logins, Terraform fmt, validate, plan, approval, apply etc.

```groovy
@Library('github.com/harisekhon/jenkins@master') _

terraformPipeline(version: '1.1.7',
                  dir: 'deployments/dev',
                  apply_branch_pattern: 'master',
                  creds: [string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')],
                  container: 'gcloud-sdk')
```

## Git Merges & Backports

Run the `gitMergePipeline` to automatically merge one branch into another, for example to automatically backport between environment branches such as any hotfixes in Staging to Dev:

```groovy
@Library('github.com/harisekhon/jenkins@master') _

gitMergePipeline('staging', 'dev')
```

That was stupidly simple wasn't it. This is the power of shared libraries.

## More Documentation

Read the comments at the top of each library function under [vars/](https://github.com/HariSekhon/Jenkins/tree/master/vars)`<function>.groovy` for more details.

If you want to prevent changes to this library re-triggering the last run of your pipelines, configure it as a a Shared Library in your global Jenkins configuration and untick "Include @Library changes in job recent changes".

See this [Jenkins Documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries) for more details.

## Production

Fork this repo to have full control over all updates.

Create environment branches to stage updates across dev/staging/production.

Enable the [fork-sync](https://github.com/HariSekhon/Jenkins/blob/master/.github/workflows/fork-sync.yaml) github actions workflow in your fork to keep the master branch sync'd every few hours.

Enable the [fork-update-pr](https://github.com/HariSekhon/Jenkins/blob/master/.github/workflows/fork-update-pr.yaml) github actions workflow to raise GitHub Pull Requests for your environment branches to audit, authorize & control updates.

## Related Repositories

- [GitHub-Actions](https://github.com/HariSekhon/GitHub-Actions) - GitHub Actions master template & GitHub Actions Shared Workflows library

- [Templates](https://github.com/HariSekhon/Templates) - Code & Config templates for many popular DevOps technologies

- [DevOps Bash Tools](https://github.com/HariSekhon/DevOps-Bash-tools) - 800+ DevOps Bash Scripts, Advanced `.bashrc`, `.vimrc`, `.screenrc`, `.tmux.conf`, `.gitconfig`, CI configs & Utility Code Library - AWS, GCP, Kubernetes, Docker, Kafka, Hadoop, SQL, BigQuery, Hive, Impala, PostgreSQL, MySQL, LDAP, DockerHub, Jenkins, Spotify API & MP3 tools, Git tricks, GitHub API, GitLab API, BitBucket API, Code & build linting, package management for Linux / Mac / Python / Perl / Ruby / NodeJS / Golang, and lots more random goodies

- [SQL Scripts](https://github.com/HariSekhon/SQL-scripts) - 100+ SQL Scripts - PostgreSQL, MySQL, AWS Athena, Google BigQuery

- [Kubernetes configs](https://github.com/HariSekhon/Kubernetes-configs) - Kubernetes YAML configs - Best Practices, Tips & Tricks are baked right into the templates for future deployments

- [Terraform](https://github.com/HariSekhon/Terraform) - Terraform templates for AWS / GCP / Azure / GitHub management

- [DevOps Python Tools](https://github.com/HariSekhon/DevOps-Python-tools) - 80+ DevOps CLI tools for AWS, GCP, Hadoop, HBase, Spark, Log Anonymizer, Ambari Blueprints, AWS CloudFormation, Linux, Docker, Spark Data Converters & Validators (Avro / Parquet / JSON / CSV / INI / XML / YAML), Elasticsearch, Solr, Travis CI, Pig, IPython

- [DevOps Perl Tools](https://github.com/harisekhon/perl-tools) - 25+ DevOps CLI tools for Hadoop, HDFS, Hive, Solr/SolrCloud CLI, Log Anonymizer, Nginx stats & HTTP(S) URL watchers for load balanced web farms, Dockerfiles & SQL ReCaser (MySQL, PostgreSQL, AWS Redshift, Snowflake, Apache Drill, Hive, Impala, Cassandra CQL, Microsoft SQL Server, Oracle, Couchbase N1QL, Dockerfiles, Pig Latin, Neo4j, InfluxDB), Ambari FreeIPA Kerberos, Datameer, Linux...

- [The Advanced Nagios Plugins Collection](https://github.com/HariSekhon/Nagios-Plugins) - 450+ programs for Nagios monitoring your Hadoop & NoSQL clusters. Covers every Hadoop vendor's management API and every major NoSQL technology (HBase, Cassandra, MongoDB, Elasticsearch, Solr, Riak, Redis etc.) as well as message queues (Kafka, RabbitMQ), continuous integration (Jenkins, Travis CI) and traditional infrastructure (SSL, Whois, DNS, Linux)

- [HAProxy Configs](https://github.com/HariSekhon/HAProxy-configs) - 80+ HAProxy Configs for Hadoop, Big Data, NoSQL, Docker, Elasticsearch, SolrCloud, HBase, Cloudera, Hortonworks, MapR, MySQL, PostgreSQL, Apache Drill, Hive, Presto, Impala, ZooKeeper, OpenTSDB, InfluxDB, Prometheus, Kibana, Graphite, SSH, RabbitMQ, Redis, Riak, Rancher etc.

- [Dockerfiles](https://github.com/HariSekhon/Dockerfiles) - 50+ DockerHub public images for Docker & Kubernetes - Hadoop, Kafka, ZooKeeper, HBase, Cassandra, Solr, SolrCloud, Presto, Apache Drill, Nifi, Spark, Mesos, Consul, Riak, OpenTSDB, Jython, Advanced Nagios Plugins & DevOps Tools repos on Alpine, CentOS, Debian, Fedora, Ubuntu, Superset, H2O, Serf, Alluxio / Tachyon, FakeS3
