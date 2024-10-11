# Jenkins - Advanced Jenkinsfile & Groovy Shared Library

[![GitHub stars](https://img.shields.io/github/stars/HariSekhon/Jenkins?logo=github)](https://github.com/HariSekhon/Jenkins/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/HariSekhon/Jenkins?logo=github)](https://github.com/HariSekhon/Jenkins/network)
[![Lines of Code](https://img.shields.io/badge/lines%20of%20code-10k-lightgrey?logo=codecademy)](https://github.com/HariSekhon/Jenkins)
[![License](https://img.shields.io/github/license/HariSekhon/Jenkins)](https://github.com/HariSekhon/Jenkins/blob/master/LICENSE)
[![My LinkedIn](https://img.shields.io/badge/LinkedIn%20Profile-HariSekhon-blue?logo=data:image/svg%2bxml;base64,PHN2ZyByb2xlPSJpbWciIGZpbGw9IiNmZmZmZmYiIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48dGl0bGU+TGlua2VkSW48L3RpdGxlPjxwYXRoIGQ9Ik0yMC40NDcgMjAuNDUyaC0zLjU1NHYtNS41NjljMC0xLjMyOC0uMDI3LTMuMDM3LTEuODUyLTMuMDM3LTEuODUzIDAtMi4xMzYgMS40NDUtMi4xMzYgMi45Mzl2NS42NjdIOS4zNTFWOWgzLjQxNHYxLjU2MWguMDQ2Yy40NzctLjkgMS42MzctMS44NSAzLjM3LTEuODUgMy42MDEgMCA0LjI2NyAyLjM3IDQuMjY3IDUuNDU1djYuMjg2ek01LjMzNyA3LjQzM2MtMS4xNDQgMC0yLjA2My0uOTI2LTIuMDYzLTIuMDY1IDAtMS4xMzguOTItMi4wNjMgMi4wNjMtMi4wNjMgMS4xNCAwIDIuMDY0LjkyNSAyLjA2NCAyLjA2MyAwIDEuMTM5LS45MjUgMi4wNjUtMi4wNjQgMi4wNjV6bTEuNzgyIDEzLjAxOUgzLjU1NVY5aDMuNTY0djExLjQ1MnpNMjIuMjI1IDBIMS43NzFDLjc5MiAwIDAgLjc3NCAwIDEuNzI5djIwLjU0MkMwIDIzLjIyNy43OTIgMjQgMS43NzEgMjRoMjAuNDUxQzIzLjIgMjQgMjQgMjMuMjI3IDI0IDIyLjI3MVYxLjcyOUMyNCAuNzc0IDIzLjIgMCAyMi4yMjIgMGguMDAzeiIvPjwvc3ZnPgo=)](https://www.linkedin.com/in/HariSekhon/)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/HariSekhon/Jenkins?logo=github)](https://github.com/HariSekhon/Jenkins/commits/master)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/48126b1db0ed4471a9888012b1ccab73)](https://app.codacy.com/gh/HariSekhon/Jenkins/dashboard)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=HariSekhon_Jenkins&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=HariSekhon_Jenkins)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=HariSekhon_Jenkins&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=HariSekhon_Jenkins)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=HariSekhon_Jenkins&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=HariSekhon_Jenkins)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=HariSekhon_Jenkins&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=HariSekhon_Jenkins)

[![CI Builds Overview](https://img.shields.io/badge/CI%20Builds-Overview%20Page-blue?logo=circleci)](https://harisekhon.github.io/CI-CD/)
[![Repo on GitHub](https://img.shields.io/badge/repo-GitHub-2088FF?logo=github)](https://github.com/HariSekhon/Jenkins)
[![Repo on GitLab](https://img.shields.io/badge/repo-GitLab-FCA121?logo=gitlab)](https://gitlab.com/HariSekhon/Jenkins)
[![Repo on Azure DevOps](https://img.shields.io/badge/repo-Azure%20DevOps-0078D7?logo=azure%20devops)](https://dev.azure.com/harisekhon/GitHub/_git/Jenkins)
[![Repo on BitBucket](https://img.shields.io/badge/repo-BitBucket-0052CC?logo=bitbucket)](https://bitbucket.org/HariSekhon/Jenkins)

[![Jenkinsfile](https://github.com/HariSekhon/Jenkins/actions/workflows/jenkinsfile.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/jenkinsfile.yaml)
[![Groovy](https://github.com/HariSekhon/Jenkins/actions/workflows/groovyc.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/groovyc.yaml)
[![YAML](https://github.com/HariSekhon/Jenkins/actions/workflows/yaml.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/yaml.yaml)
[![Markdown](https://github.com/HariSekhon/Jenkins/actions/workflows/markdown.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/markdown.yaml)
[![Validation](https://github.com/HariSekhon/Jenkins/actions/workflows/validate.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/validate.yaml)
[![Grype](https://github.com/HariSekhon/Jenkins/actions/workflows/grype.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/grype.yaml)
[![Kics](https://github.com/HariSekhon/Jenkins/actions/workflows/kics.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/kics.yaml)
[![Semgrep](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep.yaml)
[![Semgrep Cloud](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep-cloud.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/semgrep-cloud.yaml)
[![SonarCloud](https://github.com/HariSekhon/Jenkins/actions/workflows/sonarcloud.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/sonarcloud.yaml)
[![Trivy](https://github.com/HariSekhon/Jenkins/actions/workflows/trivy.yaml/badge.svg)](https://github.com/HariSekhon/Jenkins/actions/workflows/trivy.yaml)

Advanced Jenkinsfile & Jenkins Shared Library.

- [Jenkinsfile](https://github.com/HariSekhon/Jenkins/blob/master/Jenkinsfile) - epic Jenkinsfile template - full of real-world tricks from Production
- [vars/](https://github.com/HariSekhon/Jenkins/tree/master/vars) - Groovy Shared Library reusable functions - used in Production for years

Additional Jenkins scripts are available in my [HariSekhon/DevOps-Bash-tools](https://github.com/HariSekhon/DevOps-Bash-tools) repo for Jenkins Rest API and Jenkins Groovy scripts for the Admin Script Console, and Jenkins-on-Kubernetes in my [HariSekhon/Kubernetes-configs](https://github.com/HariSekhon/Kubernetes-configs) repo.

## Useful Notes

[HariSekhon/Knowledge-Base - Jenkins](https://github.com/HariSekhon/Knowledge-Base/blob/main/jenkins.md)

[HariSekhon/Knowledge-Base - Jenkins-on-Kubernetes](https://github.com/HariSekhon/Knowledge-Base/blob/main/jenkins-on-kubernetes.md)

## QuickStart

Jenkinsfile:

```groovy
// load this library straight from github - the '_' at the end imports all functions
@Library('github.com/harisekhon/jenkins@master') _

pipeline {
  stages {

    stage('Simple Example'){
      steps {
        // call any function from this libary by its filename under vars/... without the .groovy extension
        //
        // see each var/<function>.groovy file for any arguments
        //
        // calls vars/printEnv.groovy
        printEnv()

        // run logins for anything you have environment variable secrets/tokens for,
        // including AWS, GCP, DockerHub, GHCR, ECR, GCR, GAR, ACR, GitLab, Quay
        // see examples of individual service login functions in the next Stage
        login()

        // show all the cloud systems you're logged in to and who you're logged in as
        printAuth()

        // uses whichever package manager is available - portable, used by other functions too
        installPackages(['curl', 'unzip'])

        // launch a GCP Cloud Build job, by default against your cloudbuild.yaml if no args given
        gcpCloudBuild()

        // download tools to $HOME/bin
        downloadTerraform('1.2.3')
        downloadJenkinsCLI()

        // prompts for human click approval before proceeding to next step ie. production deployment
        approval()

        // GitOps update docker image version for app1 & app2 in Kubernetes Kustomize
        gitKustomizeImage(['myrepo/app1', 'myrepo/app2'])

        // trigger ArgoCD deployment to Kubernetes for app 'my-app'
        argoDeploy('my-app')

        // see groovy files under vars/ for more documentation, details and many more useful functions
      }
    }
  }

  // send notifications on broken builds and recoveries
  post {
    failure {
      // finds Git committers who broke build,
      // resolves their Slack user IDs and
      // actively notifies them with @user1 @user2 tags
      slackNotify()
    }
    fixed {
      // calls one or more notify functions to send Slack messages, emails etc.
      // such as slackNotify()
      // Uppercase N because lowercase clashes with java keyword
      // Use Notify() instead of multiple calls to different notify functions
      Notify()
    }
  }
}
```

some slightly more advanced functions:

```groovy
@Library('github.com/harisekhon/jenkins@master') _

pipeline {
  stages {
    stage('Advanced Example'){
      steps {
        // run individual login functions instead of login()

        // log in to GCP cloud with a service account key
        gcpActivateServiceAccount()
        // set up GOOGLE_APPLICATION_CREDENTIALS keyfile for 3rd party apps like Terraform
        gcpSetupApplicationCredentials()

        // log in to DockerHub
        dockerLogin()

        // log in to AWS Elastic Container Registry
        dockerLoginECR()

        // log in to Google Container Registry
        dockerLoginGCR()

        // flexible custom targeted binary downloads instead of convenience functions like downloadTerraform(), downloadJenkinsCLI():
        //
        // download, extract and install a specific version of a binary to /usr/local/bin if root or $HOME/bin if run as a user
        // here ${version} is a variable previously defined, while {os} and {arch} with no dollar sign are auto-inferred placeholders
        installBinary(url: "https://releases.hashicorp.com/terraform/${version}/terraform_${version}_{os}_{arch}.zip", binary: 'terraform')
        installBinary(url: "$JENKINS_URL/jnlpJars/jenkins-cli.jar")

        // run a script with locks to prevent another script or deployment happening at same time
        // newer runs will wait to acquire the locks, older pending runs will be skipped
        // third arg is optional to time out this script after 30 minutes
        scriptLockExecute('/path/to/script.sh', ['deployment lock', 'script lock'], 30)

        // GitOps update docker image version for app1 & app2 in Kubernetes Kustomize, images served from GCR registry
        gitKustomizeImage(["$GCR_REGISTRY/$GCR_PROJECT/app1", "$GCR_REGISTRY/$GCR_PROJECT/app2"])

        // parallelizes deployments by triggering syncs before deployment wait
        // if you want to save an extra 30 secs, use 2 parallel stages for these 2 syncs
        argoSync('app1')
        argoSync('app2')

        // waits on each app being fully deployed and passing healthchecks
        argoDeploy('app1')
        argoDeploy('app2')
      }
    }
  }
}
```

## Ready Made Pipeline Templates

### GCP CloudBuild and Deploy Docker Images to Kubernetes via ArgoCD

Builds Docker images and deploys them to [Kubernetes](https://github.com/HariSekhon/Kubernetes-configs) via [ArgoCD](https://github.com/HariSekhon/Kubernetes-configs/tree/master/argocd/base). Optionally scans the repo code, built container images, and purges Cloudflare Cache.

```groovy
@Library('github.com/harisekhon/jenkins@master') _

gcpDeployKubernetesPipeline(
  project: 'my-gcp-project',
  region: 'europe-west2',
  app: 'my-app',
  env: 'uk-production',
  images: [
    "my-app-webapp",
    "my-app-sidecar",
  ],
  gcr_registry: 'eu.gcr.io',
  gcp_serviceaccount_key: 'jenkins-gcp-serviceaccount-key',  // Jenkins credential id
  cloudflare_email: 'my-cicd-account@domain.co.uk',       // optional, triggers Cloudflare Cache Purge
  cloudflare_zone_id: '12a34b5c6d7ef8a901b2c3def45ab6c7', // if both these are set and Jenkins 'cloudflare-api-key' credential is available
)
```

![](https://github.com/HariSekhon/Diagrams-as-Code/blob/master/screenshots/gcp_cloudbuild_deployed_after_container_scans_failed.png)

See [gcpDeployKubernetesPipeline.groovy](https://github.com/HariSekhon/Jenkins/blob/master/vars/gcpDeployKubernetesPipeline.groovy) for more details, options etc.

See [Jenkins on Kubernetes Diagram](#jenkins-on-kubernetes-diagram) further down.

### Terraform CI/CD

Handles all logins, Terraform fmt, validate, plan, approval, apply etc.

Non-apply branches do Plan only so you can see if you want to merge.

On the apply branch, eg. `master` or `main`, only prompts for approval is there are actual changes in the Terraform plan output.

Saves the Terraform plan output and an approval will only apply that exact plan for safety.

```groovy
@Library('github.com/harisekhon/jenkins@master') _

terraformPipeline(version: '1.1.7',
                  dir: 'deployments/dev',
                  apply_branch_pattern: 'master',
                  creds: [
                    string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')
                  ],
                  container: 'gcloud-sdk',
                  yamlFile: 'ci/kubernetes-agent-pod.yaml')
```

Applied, ignoring informational fmt check:

![](https://github.com/HariSekhon/Diagrams-as-Code/blob/master/screenshots/terraform_applied_but_failed_fmt_check.png)

Plan found no changes so skipped Apply or asking for Approval:

![](https://github.com/HariSekhon/Diagrams-as-Code/blob/master/screenshots/terraform_plan_no_changes.png)

Plan found changes but Approval was not authorized, so Apply did not proceed:

![](https://github.com/HariSekhon/Diagrams-as-Code/blob/master/screenshots/terraform_not_approved.png)

<https://github.com/HariSekhon/Terraform>

### Git Merges & Backports

Automatically merge one branch into another upon any change eg. backport between environment branches such as any hotfixes in Staging to Dev:

```groovy
@Library('github.com/harisekhon/jenkins@master') _

// git merge from staging branch into dev branch
gitMergePipeline('staging', 'dev')
```

### Git Update Jenkinsfile Library Tag

Enumerates all Jenkins Jobs and Git Tags and Branches to give user a pop-up with parameter choices about which Pipeline's Jenkinsfile to update its `@Library` tag for, and optionally build that pipeline afterwards.

```groovy
@Library('jenkins@master') _

jenkinsfileLibraryUpdatePipeline(
    env: [
      "JENKINS_USER_ID=hari@domain.co.uk",
      "JENKINS_CLI_ARGS=-webSocket"
    ],
    creds: [string(credentialsId: 'job-config-backups', variable: 'JENKINS_API_TOKEN')],
    container: 'gcloud-sdk',
    yamlFile: 'ci/kubernetes-agent-pod.yaml'
)
```

![](https://github.com/HariSekhon/Diagrams-as-Code/blob/master/screenshots/jenkinsfile_update_library_tag_update_jenkinsfile.png)

### Jenkins Job Configuration Backups

Download and commit all Jenkins job configurations to the calling Git repo every 3 hours (configurable via optional `cron: '...'` parameter)

```groovy
@Library('github.com/harisekhon/jenkins@master') _

jenkinsBackupJobConfigsPipeline(
  dir: 'jobs',  // directory in current repo to download and git commit to
  env: [
    "JENKINS_USER_ID=hari@mydomain.co.uk",
    "JENKINS_CLI_ARGS=-webSocket"   // -webSocket gets through reverse proxies like Kubernetes Ingress
  ],
  creds: [
    string(credentialsId: 'jenkins-api-token', variable: 'JENKINS_API_TOKEN')
  ],
  container: 'gcloud-sdk',
  yamlFile: 'ci/kubernetes-agent-pod.yaml')
)
```

![](https://github.com/HariSekhon/Diagrams-as-Code/blob/master/screenshots/jenkins_job_config_backups.png)

## More Documentation

Read the comments at the top of each library function under [vars/](https://github.com/HariSekhon/Jenkins/tree/master/vars)`<function>.groovy` for more details.

If you want to prevent changes to this library re-triggering the last run of your pipelines, configure it as a a Shared Library in your global Jenkins configuration and untick "Include @Library changes in job recent changes".

See this [Jenkins Documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries) for more details.

## Jenkins on Kubernetes Diagram

![](https://raw.githubusercontent.com/HariSekhon/Diagrams-as-Code/master/images/jenkins_kubernetes_cicd.svg)

For more excellent diagrams like this, see my Diagrams-as-Code repo:

<https://github.com/HariSekhon/Diagrams-as-Code>

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

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=HariSekhon/Jenkins&type=Date)](https://star-history.com/#HariSekhon/Jenkins&Date)

## More Core Repos

<!-- OTHER_REPOS_START -->

### Knowledge

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Knowledge-Base&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Knowledge-Base)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Diagrams-as-Code&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Diagrams-as-Code)

<!--

Not support on GitHub Markdown:

<iframe src="https://raw.githubusercontent.com/HariSekhon/HariSekhon/main/knowledge.md" width="100%" height="500px"></iframe>

Does nothing:

<embed src="https://raw.githubusercontent.com/HariSekhon/HariSekhon/main/knowledge.md" width="100%" height="500px" />

-->

### DevOps Code

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=DevOps-Bash-tools&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/DevOps-Bash-tools)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=DevOps-Python-tools&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/DevOps-Python-tools)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=DevOps-Perl-tools&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/DevOps-Perl-tools)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=DevOps-Golang-tools&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/DevOps-Golang-tools)

<!--
[![Gist Card](https://github-readme-stats.vercel.app/api/gist?id=f8f551332440f1ca8897ff010e363e03)](https://gist.github.com/HariSekhon/f8f551332440f1ca8897ff010e363e03)
-->

### Containerization

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Kubernetes-configs&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Kubernetes-configs)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Dockerfiles&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Dockerfiles)

### CI/CD

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=GitHub-Actions&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/GitHub-Actions)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Jenkins&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Jenkins)

### DBA - SQL

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=SQL-scripts&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/SQL-scripts)

### DevOps Reloaded

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Nagios-Plugins&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Nagios-Plugins)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=HAProxy-configs&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/HAProxy-configs)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Terraform&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Terraform)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Packer-templates&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Packer-templates)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Nagios-Plugin-Kafka&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Nagios-Plugin-Kafka)

### Templates

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Templates&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Templates)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Template-repo&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Template-repo)

### Misc

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Spotify-tools&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Spotify-tools)
[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=HariSekhon&repo=Spotify-playlists&theme=ambient_gradient&description_lines_count=3)](https://github.com/HariSekhon/Spotify-playlists)

The rest of my original source repos are
[here](https://github.com/HariSekhon?tab=repositories&q=&type=source&language=&sort=stargazers).

Pre-built Docker images are available on my [DockerHub](https://hub.docker.com/u/harisekhon/).

<!-- 1x1 pixel counter to record hits -->
![](https://hit.yhype.me/github/profile?user_id=2211051)

<!-- OTHER_REPOS_END -->
