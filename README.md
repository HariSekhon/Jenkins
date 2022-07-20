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

        // log in to AWS Elastic Container Registry
        dockerLoginECR()

        // log in to Google Container Registry
        dockerLoginGCR()

        // show all your authentications and who you're logged in as
        printAuth()

        // launch a GCP Cloud Build job, by default against your cloudbuild.yaml if no args given
        gcpCloudBuild()

        // run a script with locks to prevent another script or deployment happening at same time
        // newer runs will wait to acquire the locks, older pending runs will be skipped
        // third arg is optional to time out this script after 30 minutes
        scriptLockExecute('/path/to/script.sh', ['deployment lock', 'script lock'], 30)

        // download tools to $HOME/bin
        downloadTerraform('1.2.3')
        downloadJenkinsCLI()
        // OR
        // download, extract and install a specific version of a binary to /usr/local/bin if root or $HOME/bin if run as a user
        // here ${version} is a variable previously defined, while {os} and {arch} with no dollar sign are auto-inferred placeholders
        installBinary(url: "https://releases.hashicorp.com/terraform/${version}/terraform_${version}_{os}_{arch}.zip", binary: 'terraform')
        installBinary(url: "$JENKINS_URL/jnlpJars/jenkins-cli.jar")

        // prompts for human click approval before proceeding to next step ie. production deployment
        approval()

        // GitOps update docker image version for app1 & app2 in Kubernetes Kustomize, images served from GCR registry
        gitKustomizeImage(["$GCR_REGISTRY/$GCR_PROJECT/app1", "$GCR_REGISTRY/$GCR_PROJECT/app2"])

        // deploy to Kubernetes via ArgoCD
        argoDeploy('my-app')

        // see groovy files under vars/ for more documentation, details and many more useful functions
      }
    }
  }
  post {
    failure {
      Notify() // DRY wrapper function to sends notifications like Slack messages, emails etc. Uppercase N because lowercase clashes with java keyword
    }
    fixed {
      Notify()
    }
  }
}
```

## Terraform CI/CD

Handles all logins, Terraform fmt, validate, plan, approval, apply etc.

Non-apply branches do Plan only so you can see if you want to merge.

On the apply branch, eg. `master` or `main`, only prompts for approval is there are actual changes in the Terraform plan output.

Saves the Terraform plan output and an approval will only apply that exact plan for safety.

```groovy
@Library('github.com/harisekhon/jenkins@master') _

terraformPipeline(version: '1.1.7',
                  dir: 'deployments/dev',
                  apply_branch_pattern: 'master',
                  creds: [string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')],
                  container: 'gcloud-sdk')
```

## Git Merges & Backports

Automatically merge one branch into another upon any change eg. backport between environment branches such as any hotfixes in Staging to Dev:

```groovy
@Library('github.com/harisekhon/jenkins@master') _

gitMergePipeline('staging', 'dev')
```

## Jenkins Job Configuration Backups

Download and commit all Jenkins job configurations to the calling Git repo every 3 hours (configurable via optional `cron: '...'` parameter)

```groovy
@Library('github.com/harisekhon/jenkins@master') _

jenkinsBackupJobConfigsPipeline(
  dir: 'jobs',  // directory in current repo to download and git commit to
  env: ["JENKINS_USER_ID=hari@mydomain.co.uk", "JENKINS_CLI_ARGS=-webSocket"],  // -webSocket gets through reverse proxies like Kubernetes Ingress
  creds: [string(credentialsId: 'jenkins-api-token', variable: 'JENKINS_API_TOKEN')],
  container: 'gcloud-sdk')
)
```

## More Documentation

Read the comments at the top of each library function under [vars/](https://github.com/HariSekhon/Jenkins/tree/master/vars)`<function>.groovy` for more details.

If you want to prevent changes to this library re-triggering the last run of your pipelines, configure it as a a Shared Library in your global Jenkins configuration and untick "Include @Library changes in job recent changes".

See this [Jenkins Documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries) for more details.

## Production

### Option 1 - Hashref

Import the library as shown above directly from this repo, replacing `@master` with `@<hashref>` to fix to an immutable version (tags are not immutable). This is a GitHub security best practice for CI/CD as seen in this [doc](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#using-third-party-actions).

### Option 2 - Public Fork (fully automated)

Fork this repo for more control and visibility over all updates.

Enable the [fork-sync](https://github.com/HariSekhon/Jenkins/blob/master/.github/workflows/fork-sync.yaml) github actions workflow in your fork to keep the master branch sync'd every few hours.

You can then create tags or environment branches to stage updates across dev/staging/production.

If using environment branches, enable the [fork-update-pr](https://github.com/HariSekhon/Jenkins/blob/master/.github/workflows/fork-update-pr.yaml) github actions workflow to automatically raise GitHub Pull Requests for your environment branches to audit, authorize & control updates.

### Option 3 - Private Copy (semi-automated)

Download the functions you want into your private jenkins shared library repo.

You can use the [vars/download.sh](https://github.com/HariSekhon/Jenkins/blob/master/vars/download.sh) script to help you download given `*.groovy` files and periodically run it to get updates to these previously downloaded functions.

You will be responsible for committing and reconciling any divergences in your local copies.

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

### Stargazers over time

[![Stargazers over time](https://starchart.cc/HariSekhon/Jenkins.svg)](https://starchart.cc/HariSekhon/Jenkins)
