//  vim:ts=2:sts=2:sw=2:et:filetype=groovy:syntax=groovy
//
//  Author: Hari Sekhon
//  Date: [% DATE # 2017-06-28 12:39:02 +0200 (Wed, 28 Jun 2017) %]
//
//  [% URL  # https://github.com/HariSekhon/Jenkins %]
//
//  [% LICENSE %]
//
//  [% MESSAGE %]
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//                        J e n k i n s   P i p e l i n e
// ========================================================================== //

// https://jenkins.io/doc/book/pipeline/syntax/

// Validated on Jenkins LTS 2.263.x, 2.277.x, 2.289.x, 2.303.x, 2.319.x
//
//    see check_jenkinsfile.sh in my DevOps Bash tools repo:
//
//      https://github.com/HariSekhon/DevOps-Bash-tools/


// ========================================================================== //
//                        S h a r e d   L i b r a r i e s
// ========================================================================== //

// https://www.jenkins.io/doc/book/pipeline/shared-libraries/

// XXX: see vars/ directory in this repo for Shared Libary functions

// import a preconfigured shared library to use its functions for code reuse
//@Library(value="name@version", changelog=true|false) _ // changelog will not only include lib changes in changeset but also trigger new builds when the library changes, not very scalable to auto-re-run all your pipelines
//@Library(value='harisekhon@master', changelog=false) _
//@Library('namedlibrary@master') _
// // import 2 libraries
//@Library(['namedlibrary', 'otherlib@tag']) _
//
// load directly from this GitHub repo without having to configure a Named Shared Library in the Jenkins global configuration
// XXX: implicitly causes "Include @Library changes in job recent changes" behaviour and triggering when the library changes
@Library('github.com/harisekhon/jenkins@master') _

// more dynamic but $BRANCH_NAME is only available in a Jenkins MultiBranch Pipeline
//library "namedlibrary@$BRANCH_NAME"


pipeline {

  // ========================================================================== //
  //                                  A g e n t s
  // ========================================================================== //

  // agent setting is required otherwise build won't ever run
  // run pipeline on any agent
  agent any
  // can override this on a per stage basis, but leaving this as "any" will incur some overhead on the Jenkins master in that case, better to switch whole pipeline to a remote agent if you can, unless you want to parallelize the stages among different agents, which might be especially useful for on-demand cloud agents run in Kubernetes
  // putting agents{} sections in stage will also have options applied to the agent eg. timeout includes agent provisioning time

  // more fancy agents - Docker or Kubernetes
//  agent {
    //
    // =========================================
    //                Docker
    //
    //    https://www.jenkins.io/doc/book/pipeline/docker/
    //
    // run pipeline inside a docker container (requires the jenkins agent has native docker command and docker access - not available by default in jenkins docker images), see:
    //
    //  https://github.com/HariSekhon/Dockerfiles/tree/master/jenkins-agent-docker
    //  https://github.com/HariSekhon/Kubernetes-templates/blob/master/jenkins-agent.cloud-pod-DooD.yaml
    //
    // put an agent { docker {...} } section in each stage to use different images with different available tools
//    docker {
//      image 'ubuntu:18.04'
//      args '-v $HOME/.m2:/root/.m2 -v $HOME/.sbt:/root/.sbt -v $HOME/.ivy2:/root/.ivy2 -v $HOME/.gradle:/root/.gradle -v $HOME:/.groovy:/root/.groovy -v $HOME/.cache/pip:/root/.cache/pip -v $HOME/.cpan:/root/.cpan -v $HOME/.cpanm:/root/.cpanm -v $HOME/.gem:/root/.gem'
//    }

    // =========================================
    //                Kubernetes
    //
    //    https://plugins.jenkins.io/kubernetes/
    //
    // run pipeline in a k8s pod, can choose different containers in stages further down
//    kubernetes {
//      defaultContainer 'gcloud-sdk'  // default container the build executes in, otherwise uses jnlp by default which doesn't have the right tooling
//      idleMinutes 5  // keep alive for 5 mins to reuse if a new build is triggered in that time
//      //label 'jenkins-agent' // prefix name for k8s pod - defaults to <pipeline>-<buildnumber>-<randomhash>
//      //runAsUser <uid>
//      //runAsGroup <gid>
//      //
//      // XXX: use external yaml rather than inline pod spec - better for yaml validation and sharing between pipelines
//      // XXX: See more advanced example including pod scheduling avoiding preemptible nodes here:
//      //
//      //    https://github.com/HariSekhon/Kubernetes-templates/blob/master/jenkins-agent-pod.yaml
//      //
//      yamlFile 'jenkins-agent-pod.yaml'  // relative to root of repo
//      // or inline:
//      //yaml """\
//      //  apiVersion: v1
//      //  kind: Pod
//      //  metadata:
//      //    namespace: jenkins
//      //    labels:
//      //      app: gcloud-sdk
//      //  spec:
//      //    containers:
//      //      - name: gcloud-sdk  # do not name this 'jnlp', without that container this'll never come up properly to execute the build
//      //        image: gcr.io/google.com/cloudsdktool/cloud-sdk:latest
//      //        command:
//      //          - cat
//      //        tty: true
//      //        resources:
//      //          requests:
//      //            cpu: 300m     # actually takes 800m but overcontend rather than spawning too many nodes for bursty workload
//      //            memory: 300Mi # uses around 250Mi
//      //          limits:
//      //            cpu: "1"
//      //            memory: 1Gi
//      //      # more containers if you want to run different stages in different containers eg. to wget -O- | jq ...
//      //      - name: jq
//      //        image: stedolan/jq
//      //        command:
//      //          - cat
//      //        tty: true
//      //        resources:
//      //          requests:
//      //            cpu: 100m
//      //            memory: 50Mi
//      //          limits:
//      //            cpu: 500m
//      //            memory: 500Mi
//      //    """.stripIndent()
//     }
//  }

  //tools {
  //  jdk 'my-jdk'  // configure specific JDK versions under Global Tool Configuration
  //  jfrog 'jfrog-cli'  // set this up in Manage Jenkins -> Global Tool Configuration
  //}

  // ========================================================================== //
  //                                 O p t i o n s
  // ========================================================================== //

  options {
    // only allow 1 of this pipeline to run at a time - usually better to just lock some stages eg. see Deploy stage further down
    //disableConcurrentBuilds()

    // put timestamps in console logs
    timestamps()

    // timeout entire pipeline after 2 hours
    // XXX: if using Approval input in prod pipeline, if you don't confirm the deployment within under 2 hours this build will be cancelled to avoid hogging executors
    timeout(time: 2, unit: 'HOURS')

    //retry entire pipeline 3 times, better to do retry at each Stage / Step for efficiency to not repeat previously succeed steps
    // XXX: this also fails Milestone ordinals when repeating previous Stage / Step, cannot go backwards
    //retry(3)

    // https://www.jenkins.io/doc/book/pipeline/syntax/#parallel
    parallelsAlwaysFailFast()

    // do not allow more than 3 builds to run in a minute
    rateLimitBuilds(throttle: [count: 3, durationName: 'minute', userBoost: false])

    // only keep last 100 pipeline logs and artifacts
    buildDiscarder(logRotator(numToKeepStr: '100'))

    // https://www.jenkins.io/doc/pipeline/steps/gitlab-plugin/
    // enable build status feedback to GitLab
    //gitLabConnection('Gitlab')
    //gitlabCommitStatus(name: "Jenkins build $BUILD_DISPLAY_NAME")

    // requires ansicolor plugin
    ansiColor('xterm')
  }

  // https://www.jenkins.io/doc/book/pipeline/syntax/#cron-syntax
  triggers {
    // replace 0 with H (hash) to randomize starts to spread load and avoid spikes
    // the time is consistent for each job though as it's based on the hash of the job name
    //
    // XXX: GitHub Jenkins webhooks are more instant and efficient than this frequent polling
    //
    // XXX: Jenkins webhook bug - occasionally fails (rare), so poll GitHub / SCM as a backup to trigger
    //
    //      https://issues.jenkins.io/browse/JENKINS-50154
    //
    pollSCM('H/10 * * * *')  // run every 10 mins, at a consistent offset time within that 10 min interval

    cron('H 10 * * 1-5')  // run at 10:XX:XX am every weekday morning, ie. some job fixed time between 10-11am
    //cron('@hourly')       // same as cron('H * * * *')
    //cron('@daily')        // same as cron('H H * * *')
  }

  // need to specify at least one env var if enabling
  //environment {
  //  //CC = 'clang',
  //  DEBUG = '1'
  //}

  //parameters {
  //  // access this using ${params.MyVar} elsewhere in build stages
  //  string(
  //    name: 'MyVar',
  //    defaultValue: 'MyString',
  //    description: 'blah',
  //    trim: true
  //  )
  //}

//  parameters {
//    string(
//      name: 'CLASS',
//      // unfortunately this description gets collapsed to one line, and even inserting \n doesn't help
//      description: """
//Which class tests to run?
//
//(can specify .** wildcard suffix for multiple classes,
//or #mymethod suffix for exact method within class)
//""",
//      defaultValue: 'com.domain.test.admin.blah.MyTest',
//      trim: true
//    )
//  }

  // creates a drop-down list prompt in the Jenkins UI with these pre-populated choices - first choice is the default value
  //parameters {
  //  choice(
  //    name: 'PACKAGE',
  //    description: 'Which tests to run?',
  //    choices: [
  //      'com.mydomain.test.admin.**',
  //      'com.mydomain.test.main.**',
  //      'com.mydomain.test.tools.**',
  //    ]
  //  )
  //}


  // ========================================================================== //
  //               E n v i r o n m e n t   &   C r e d e n t i a l s
  // ========================================================================== //

  //    https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#handling-credentials
  //
  // XXX: do not allow untrusted Pipeline jobs / users to use trusted Credentials as they can extract these environment variables
  //
  // these will be starred out *** in console log but user scripts can still print these
  // can move this under a stage to limit the scope of their visibility
  environment {
    // XXX: Edit - useful for scripts to know which environment they're in to make adjustments
    ENV = 'dev'
    APP = 'www' // used by scripts eg. ArgoCD app sync commands
    VERSION = "$GIT_COMMIT"
    //JDBC_URL = 'jdbc:mysql://x.x.x.x:3306/my_db'
    //JDBC_URL = 'jdbc:postgres://x.x.x.x:5432/my_db'

    // repo passed to gitKustomize to GitOps deploy via ArgoCD
    GITOPS_REPO = 'git@github.com/HariSekhon/app-k8s'

    // my DevOps Bash tools lib/ci.sh library will automatically set CI=true in shell if sourced and any of the CI heuristics match
    CI = true  // used by Artifactory JFrog CLI to disable interactive prompts and progress bar - https://www.jfrog.com/confluence/display/CLI/JFrog+CLI#JFrogCLI-EnvironmentVariables

    // used by Git branch auto-merges and GitOps K8s image version updates
    GIT_USERNAME = 'Jenkins'
    GIT_EMAIL = 'platform-engineering@MYCOMPANY.CO.UK'
    // general git command debugging
    //GIT_TRACE=1
    //GIT_TRACE_SETUP=1
    // should store these in Git for security and auditing purposes before loading to Jenkins credentials
    GITHUB_SSH_KNOWN_HOSTS = credentials('github-ssh-known-hosts')              // eg. 'ssh-keyscan github.com'
    GITLAB_SSH_KNOWN_HOSTS = credentials('gitlab-ssh-known-hosts')              // eg. 'ssh-keyscan gitlab.com'
    AZURE_DEVOPS_SSH_KNOWN_HOSTS = credentials('azure-devops-ssh-known-hosts')  // eg. 'ssh-keyscan ssh.dev.azure.com'
    BITBUCKET_SSH_KNOWN_HOSTS = credentials('bitbucket-ssh-known-hosts')        // eg. 'ssh-keyscan bitbucket.org'
    // appended to ephemeral k8s agents >> ~/.ssh/known_hosts by some shared libraries eg. gitMergePipeline.groovy, gitKustomizeImage.groovy
    SSH_KNOWN_HOSTS = """
      $GITHUB_SSH_KNOWN_HOSTS
      $GITLAB_SSH_KNOWN_HOSTS
      $AZURE_DEVOPS_SSH_KNOWN_HOSTS
      $BITBUCKET_SSH_KNOWN_HOSTS
    """

    // XXX: CAREFUL who can create CI/CD commits or PRs with this credentialled pipeline as they could obtain these credentials
    // create these credentials as Secret Text in Jenkins UI -> Manage Jenkins -> Manage Credentials -> Jenkins -> Global Credentials -> Add Credentials
    AWS_ACCESS_KEY_ID      = credentials('aws-secret-key-id')
    AWS_SECRET_ACCESS_KEY  = credentials('aws-secret-access-key')
    GCP_SERVICEACCOUNT_KEY = credentials('gcp-serviceaccount-key')
    // GOOGLE_APPLICATION_CREDENTIALS environment variable needs to be set for apps like Terraform, Trivy etc.
    // to access GCP resources, and is required if calling gcpSetupApplicationCredentials()
    GOOGLE_APPLICATION_CREDENTIALS = "$WORKSPACE_TMP/.gcloud/application-credentials.json.$BUILD_TAG"
    DIGITALOCEAN_ACCESS_TOKEN = credentials('digitalocean-access-token') // picked up automatically by Digital Ocean CLI 'doctl'

    // Username/Password credentials - creates Environment Variables of these names suffixed with _USR and _PSW
    GITHUB_TOKEN    = credentials('github-token')
    // GITHUB_TOKEN_USR
    // GITHUB_TOKEN_PSW
    AWS_ACCESS_KEY  = credentials('aws') // AWS_ACCESS_KEY_ID should be in Username field and AWS_SECRET_ACCESS_KEY in the Password field
    // AWS_ACCESS_KEY_USR
    // AWS_ACCESS_KEY_PSW
    // for better naming you can do a withCredentials block like this:
    // withCredentials([usernamePassword(credentialsId: 'aws', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')])

    AWS_ACCOUNT_ID = credentials('aws-account-id') // or better yet just generate it from access keys via 'aws sts get-caller-identity | jq -r .Account'
    AWS_DEFAULT_REGION = 'eu-west-2' // consider setting this as a global variable in Jenkins instead of in each Jenkinsfile
    AWS_ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
    //AWS_DEFAULT_OUTPUT = 'json'
    //AWS_MAX_ATTEMPTS = 3
    AWS_EKS_CLUSTER = 'mycluster'

    // https://cloud.google.com/sdk/gcloud/reference/config
    CLOUDSDK_CORE_PROJECT = 'mycompany-dev'
    CLOUDSDK_COMPUTE_REGION = 'europe-west2' // London
    //CLOUDSDK_COMPUTE_ZONE = "${env.CLOUDSDK_COMPUTE_REGION}-a" # or b or c
    //CLOUDSDK_CONTAINER_CLUSTER = myGKEcluster
    GCR_REGISTRY = 'eu.gcr.io'

    // use to purge Cloudflare Cache
    CLOUDFLARE_API_KEY = credentials('cloudflare-api-key')

    // if creating docker images on agents, this enables BuildKit which automatically creates images layers in parallel where possible (especially useful for multi-stage builds)
    // also add '--build-arg BUILDKIT_INLINE_CACHE=1' to the docker build command
    DOCKER_BUILDKIT = 1

    // AWS
    DOCKER_IMAGE = "$AWS_ECR_REGISTRY/$APP"
    // GCR
    //DOCKER_IMAGE = "$GCR_REGISTRY/$CLOUDSDK_CORE_PROJECT/$APP"
    //DOCKER_IMAGE = "$GCR_REGISTRY/$GCR_PROJECT/$APP"  // if using a shared project for image promotion across environments
    // GitHub Container Registry
    GHCR_REGISTRY = 'ghcr.io/harisekhon'
    //DOCKER_IMAGE = "$GHCR_REGISTRY/$APP"
    // DockerHub
    //DOCKER_IMAGE = "harisekhon/$APP"
    DOCKER_TAG = "$GIT_COMMIT" // or "$GIT_BRANCH" which can be set to a semver git tag
    //DOCKER_TAG = "${env.GIT_BRANCH.split('/')[-1]}"  // strip the leading 'origin/' from 'origin/mybranch'

    ARTIFACTORY_URL = 'http://x.x.x.x:8082/artifactory/'
    ARTIFACTORY_ACCESS_TOKEN = credentials('artifactory-access-token')

    // use to trigger deployment sync's for apps as deploy step
    ARGOCD_SERVER = 'argocd.domain.com'
    ARGOCD_AUTH_TOKEN = credentials('argocd-auth-token')

    // can be used by downloadJenkinsCLI() and other jenkins*() functions from vars/
    // careful, $HOME could evaluate to jenkins user in pipeline on Jenkins server but could evaluate to root in agents, especially Kubernetes ages running several pods, some of which need root
    //JENKINS_CLI_JAR = "$HOME/bin/jenkins-cli.jar"

    TF_IN_AUTOMATION = 1  // changes output to suppress CLI suggestions for related commands
    //TF_WORKSPACE = "$ENV"  // run the same automation against multiple environments

    // to use Hermit managed fixed binaries and versions, wrap steps in withEnv, splitting the hermit env output
    //withEnv( HERMIT_ENV_VARS.split('\n').toList() ) {
    HERMIT_ENV_VARS = sh(returnStdout: true, script: './bin/hermit env --raw').trim()

    // for Run Tests stage
    // reference this in double quotes to interpolate in the Jenkinsfile to display the literal value in the Blue Ocean UI step header
    // reference this in single quotes to interpolate in the shell
    // XXX: Edit
    //SELENIUM_HUB_URL = 'http://x.x.x.x:4444/wd/hub/'
    // if run on K8s through an ingress (see https://github.com/HariSekhon/Kubernetes-templates/)
    //SELENIUM_HUB_URL = 'https://x.x.x.x/wd/hub/'
    THREAD_COUNT = 6

    // Instrumentation for Observability
    //PROMETHEUS_NAMESPACE = 'default'
    //PROMETHEUS_ENDPOINT = 'prometheus'
    //COLLECTING_METRICS_PERIOD_IN_SECONDS = '120'
    //COLLECT_DISK_USAGE = 'true'  // for cloud agents set to false to avoid scanning virtually unlimited storage, or install 'CloudBees Disk Usage Simple' plugin to provide this info (done in jenkins-values.yaml in my Kubernetes repo)

    // using this only to dedupe common message suffix for Slack channel notifications in post {}
    SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    //SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}> (<${env.JOB_URL}/${env.BUILD_NUMBER}/allure/|Allure Report>)"
    //SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}> (<${env.JOB_URL}/${env.BUILD_NUMBER}/allure/|Allure Report>) - ${params.CLASS}"    // to differentiate Single Class Tests
    //SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}> (<${env.JOB_URL}/${env.BUILD_NUMBER}/allure/|Allure Report>) - ${params.PACKAGE}"  // to differentiate Single Package Tests

    // https://semgrep.dev/docs/semgrep-ci/sample-ci-configs/#jenkins
    //SEMGREP_RULES = "p/security-audit p/secrets" // more at semgrep.dev/explore
    SEMGREP_BASELINE_REF = "origin/${env.CHANGE_TARGET}"

    // Instead of `SEMGREP_RULES:`, use rules set in Semgrep App.
    // Get your token from semgrep.dev/manage/settings.
    //   SEMGREP_APP_TOKEN: credentials('SEMGREP_APP_TOKEN')
    //   SEMGREP_REPO_URL = env.GIT_URL.replaceFirst(/^(.*).git$/,'$1')
    //   SEMGREP_BRANCH = "${CHANGE_BRANCH}"
    //   SEMGREP_JOB_URL = "${BUILD_URL}"
    //   SEMGREP_REPO_NAME = env.GIT_URL.replaceFirst(/^https:\/\/github.com\/(.*).git$/, '$1')
    //   SEMGREP_COMMIT = "${GIT_COMMIT}"
    //   SEMGREP_PR_ID = "${env.CHANGE_ID}"

    // Never fail the build due to findings.
    // Instead, just collect findings for semgrep.dev/manage/findings
    //SEMGREP_AUDIT_ON = "unknown"

    // Change job timeout (default is 1800 seconds; set to 0 to disable)
    SEMGREP_TIMEOUT = "300"

  }

  // ========================================================================== //
  //                                  S t a g e s
  // ========================================================================== //

  // https://www.jenkins.io/doc/pipeline/steps/
  //
  // https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps/

  stages {

    // not usually needed when sourcing Jenkinsfile from Git SCM in Pipeline / Multibranch Pipeline this is implied
    stage ('Checkout') {
      steps {
        milestone(ordinal: null, label: "Milestone: Checkout")
        checkout(
          [
            $class: 'GitSCM',
            userRemoteConfigs: [
              [
                url: 'https://github.com/HariSekhon/DevOps-Bash-tools',
                credentialsId: '',
              ]
            ],
            branches: [
              [
                name: '*/master'
              ]
            ],
            //doGenerateSubmoduleConfigurations: false,
            //extensions: [],
            //submoduleCfg: [],
          ]
        )
        //container('git') {
        //  git credentialsId: 'GitHub', url: 'https://github.com/HariSekhon/Jenkins.git', branch: 'master'
        //}
      }
    }

    // ========================================================================== //
    //              G i t   M e r g e   B r a n c h   B a c k p o r t s
    // ========================================================================== //

    // auto-backport hotfixes to upstream environments
    stage('Git Merge') {
      // applied before stage { agent{} }
//      options {
//        // includes agent wait time, agent availability delays could induce this stage to fail this way
//        timeout(time: 1, unit: 'HOURS')
//      }

      // XXX: only works on a Multi-branch pipeline and causes Stage skip in normal Pipeline builds
      //      usually this will be something like origin/staging - use the Environment step to show the BRANCH_NAME / GIT_BRANCH
      //when { branch pattern: '^.*/staging$', comparator: 'REGEXP' }
      when {
        beforeAgent true  // don't spin up a K8s pod if we don't need to execute
        branch '*/staging'
        //changeset 'src/**/*.java'  // only run this stage if Java code changed
        //triggeredBy 'TimerTrigger'
        //triggeredBy 'GitLabWebHookCause'
        //triggeredBy cause: 'UserIdCause' // cause can be specified in a triggering script such as jenkins_job_trigger.sh in the adjacent DevOps Bash tools repo
      }

      steps {
      // XXX: move to Shared Libary to use Groovy to define lock in a String and add an informational Acquiring Lock message to make it more obvious when a build is waiting on a lock before progressing, otherwise they just look like they're hanging
      //
      // XXX: see vars/ shared library directory in this repo
      //
      //String gitMergeLock = "Git Merge '$from_branch' to '$to_branch'"
      //echo "Acquiring Git Merge Lock: $gitMergeLock"
      //lock(resource: gitMergeLock, inversePrecedence: true) {

        echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
        timeout(time: 1, unit: 'MINUTES') {
          sh script: 'whoami', label: 'User' // because $USER is sometimes not defined in env
          sh script: 'id', label: 'id'       // to compare UID / GID vs filesystem permissions
          sh script: 'env | sort', label: 'Environment'
        }
        lock(resource: 'Git Merge Staging to Dev', inversePrecedence: true) {
          milestone ordinal: null, label: "Milestone: Git Merge"
          timeout(time: 5, unit: 'MINUTES') {
            // provides credential as env vars to use as per normal eg. git clone https://$GIT_USER:$GIT_TOKEN@github.com/...
            //withCredentials([vaultString(credentialsId: 'my-secret', variable: 'MYSECRET')]) {  // HashiCorp Vault via plugin integration to give new type of vaultString
            //withCredentials([usernamePassword(credentialsId: 'jenkins-user-token-for-github', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
            // requires SSH Agent plugin + restart
            sshagent (credentials: ['jenkins-ssh-key-for-github']) {
              retry(2) {
                sh 'path/to/git_merge_branch.sh staging dev'  // script in https://github.com/HariSekhon/DevOps-Bash-tools - or see vars/gitMerge*.groovy for native shared libary
              }
            }
          }
        }
      }
    }

    // ========================================================================== //
    //                                   S e t u p
    // ========================================================================== //

    stage('Setup') {
      steps {
        milestone(ordinal: null, label: "Milestone: Setup")
        label 'Setup'
        // execute in container name defined in the kubernetes {} section near the top
        //container('gcloud-sdk') {

        // running container defined in kubernetes jenkins pod {} near top
        container('jq') {
          sh "wget -qO- ifconfig.co/json | jq -r '.ip'"
          sh 'script_using_jq.sh'
        }

        script {
          // rewrite build name to include commit id
          currentBuild.displayName = "$BUILD_DISPLAY_NAME (${GIT_COMMIT.take(8)})"

          // save workspace path to use in tests
          workspace = "$env.WORKSPACE"
        }

        // run some shell commands to set things up
        sh '''
          for x in /etc/mybuild.d/*.sh; do
            if [ -r "$x" ]; then
              source $x;
            fi;
          done;
        '''

        // DRY for gcpCloudBuild() and gitKustomize() - see further down
        // done here because you cannot run straight Groovy in the environment{} section and env also can't take list constructs which get syntactically checked,
        // you'd have to write in multiline """ string which requires both global and ${it.trim()}
        echo "Generating DOCKER_IMAGES list"
        script {
          env.DOCKER_IMAGES = [
            "$APP-php",
            "$APP-nginx",
            "$APP-cache",
            "$APP-sql-proxy",
          ].collect{"$GCR_REGISTRY/$GCR_PROJECT/$it"}.join(',')

          // useful to sending to grype() and trivyScanDockerImages() - see Container Scanning section further down
          env.DOCKER_IMAGES_TAGS = env.DOCKER_IMAGES.split(',').collect{"$it:$VERSION"}.join(',')
        }
        printEnv()
      }
    }

    stage('Auth'){
      steps {
        gcpActivateServiceAccount()
        printAuth()
      }
    }

    stage("Generate environment variable AWS_ACCOUNT_ID") {
      steps {
        script {
          env.AWS_ACCOUNT_ID = sh(script:'aws sts get-caller-identity | jq -r .Account', returnStdout: true).trim()
        }
      }
    }

    stage('Groovy version') {
      steps {
        // first install the Groovy plugin, then in Global Tool Configuration set up a Groovy version with this name '3.0.9' to select here - if already installed, just untick "Install automatically" and point GROOVY_HOME to the installation path
        //withGroovy(tool: '3.0.9', jdk: 'some-specific-jdk-tool-name'){
        withGroovy(tool: '3.0.9'){
          sh "groovy --version"
        }
      }
    }

    // not needed for Kubernetes / Docker agents as they start clean
    stage('Maven Clean') {
      //tools {
      //  jdk 'my-jdk'  // configure specific JDK versions under Global Tool Configuration
      //}
      steps {
        sh "./mvnw clean"
      }
    }

    // ========================================================================== //
    //                                    T e s t
    // ========================================================================== //

    // XXX: Stage names in Blue Ocean UI seem to truncate at around 14-15 chars on the 2nd line
    stage('Wait for Selenium Grid to be up') {
      steps {
        sh script: "./selenium_hub_wait_ready.sh '$SELENIUM_HUB_URL' 60"  // script available in DevOps-Bash-tools repo
      }
    }

    stage('Test') {
      //options {
      //  retry(2)
      //}
      steps {
        milestone(ordinal: null, label: "Milestone: Test")
        echo 'Testing...'
        timeout(time: 60, unit: 'MINUTES') {
          sh 'make test'
          // junit '**/target/*.xml'
        }
      }
    }

    // lock multiple stages into 1 concurrent execution using a parent stage
    stage('Parent') {
      options {
        lock('something')
      }
      stages {
        stage('one') {
          steps {
            sh '...'
          }
        }
        stage('two') {
          steps {
            sh '...'
          }
        }
      }
    }

    // alternative quick Pipeline to run just a single package of tests for quicker debugging and testing
    stage('Run Single Package Tests') {
      steps {
        // params.PACKAGE is populated from the parameters { choice { ... } } defined further above which creates a drop-down list prompt in Jenkins UI
        sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dtest='${params.PACKAGE}' -DthreadCount='$THREAD_COUNT'"
      }
    }

    stage('Run Tests in Parallel') {
      parallel {
        stage('Run Desktop Tests') {
          steps {
            sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dgroups=com.mydomain.category.interfaces.DesktopTests -DthreadCount='$THREAD_COUNT'"
          }
        }

        stage('Run Mobile Tests') {
          steps {
            sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dgroups=com.mydomain.category.interfaces.MobileTests -Dmobile=true -DthreadCount='$THREAD_COUNT'"
          }
        }
      }
    }

    // ========================================================================== //
    // Run Tests in Serial - continue to Mobile tests regardless of whether Desktop tests pass so that we can get full Allure Report
    stage('Run Desktop Tests') {
      steps {
        // continue to Mobile tests regardless of whether this stage fails, will still mark the build to failed though
        catchError (buildResult: 'FAILURE', stageResult: 'FAILURE') {  // set stage to failed too, not just build
          sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dgroups=com.mydomain.category.interfaces.DesktopTests -DthreadCount='$THREAD_COUNT'"
        }
      }
    }

    stage('Run Mobile Tests') {
      steps {
        sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dgroups=com.mydomain.category.interfaces.MobileTests -Dmobile=true -DthreadCount='$THREAD_COUNT'"
      }
    }

    // ========================================================================== //
    //                       J F r o g   A r t i f a c t o r y
    // ========================================================================== //

    // jf function is equivalent to running jfrog-cli

    stage('JFrog Artifactory') {
      steps {
        // show version
        jf '-v'

        // check connecitivity
        jf 'rt ping'

        // upload
        jf 'rt u target/ my-repo/'
      }
    }

    // ========================================================================== //
    //                         S u r e f i r e   R e p o r t
    // ========================================================================== //

    stage('Surefire Report'){
      steps {
        sh './mvnw clean install site surefire-report:report'
        // Requires HTML Publisher plugin
        publishHTML([
          allowMissing: false,
          alwaysLinkToLastBuild: true,
          keepAll: false,
          reportDir: 'target/site',
          reportFiles: 'surefire-report.html',
          reportName: 'Surefire Report',
          reportTitles: '',
          useWrapperFileDirectory: true
        ])
      }
    }

    // ========================================================================== //
    //                               S o n a r Q u b e
    // ========================================================================== //

    stage('SonarQube Scan'){
      steps {
        withSonarQubeEnv(installationName: 'mysonar'){  // configure with details of SonarQube installation
          sh './mvnw clean org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar'
        }
      }
    }

    stage('SonarQube Quality Gate'){
      steps {
        timeout(time: 2, unit: 'MINUTES'){
          waitForQualtityGate abortPipeline: true
        }
      }
    }

    // ========================================================================== //
    //                                 C h e c k o v
    // ========================================================================== //

    stage('Checkov') {
      when {
        // Scan changed files in PRs, block on new issues only (existing issues ignored)
        expression { env.CHANGE_ID && env.BRANCH_NAME.startsWith("PR-") }
        beforeAgent true
      }
      steps {
        checkov()
      }
    }

    // ========================================================================== //
    //                                 S e m g r e p
    // ========================================================================== //

    stage('Semgrep') {
      when {
        // Scan changed files in PRs, block on new issues only (existing issues ignored)
        expression { env.CHANGE_ID && env.BRANCH_NAME.startsWith("PR-") }
        beforeAgent true
      }
      steps {
        container('semgrep'){
          steps {
            sh 'git fetch origin ${SEMGREP_BASELINE_REF#origin/} && semgrep-agent'
          }
        }
      }
    }

    // ========================================================================== //
    //                                    K I C S
    // ========================================================================== //

    stage('KICS') {
      steps {
        //downloadKICS()  // func in vars/ shared library
        container('kics'){
          sh 'mkdir -p results'
          sh 'kics scan --ci --no-color -p ${WORKSPACE} --output-path results --ignore-on-exit results --report-formats "json,sarif,html"'
          archiveArtifacts(artifacts: 'results/*.html,results/*.sarif,results/*.json', fingerprint: true)
        }
      }
    }

    // ========================================================================== //
    //                              M e g a L i n t e r
    // ========================================================================== //

    stage('MegaLinter'){
      steps {
        container('megalinter'){
          steps {
            sh '/entrypoint.sh'
          }
        }
      }
    }

    // ========================================================================== //
    //                          P r e - B u i l d   H o o k
    // ========================================================================== //

    // see also: https://www.jenkins.io/doc/book/managing/groovy-hook-scripts/

    stage('Pre-Build Hook') {
      when {
        expression {
          fileExists 'jenkins/preBuildHook.sh'
        }
      }
      steps {
        sh 'jenkins/preBuildHook.sh'
      }
    }

    // ========================================================================== //
    //                                   B u i l d
    // ========================================================================== //

    // Parallelize build via multiple sub-stages if possible:
    // https://www.jenkins.io/doc/book/pipeline/syntax/#parallel

    stage('Build') {
      // only apply env to this stage
      environment {
        DEBUG = '1'
      }
      // specify agent at stage level to build on different environments
      //agent {
      //  label 'linux'
      //}

      steps {
        // forbids older builds from starting
        // XXX: do not wrap milestone in a retry (stage/multi-steps/whole pipeline) because it will fail retry even for the same ordinal number
        milestone(ordinal: null, label: "Milestone: Build")

        // convenient in Blue Ocean to see the environment quickly in a separate expand box
        //timeout(time: 1, unit: 'MINUTES') {
        //  sh script: 'env | sort', label: 'Environment'
        //}

        //echo "${params.MyVar}"
        echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
        echo 'Building...'

        cloudBuild(timeout_minutes=40)  // func in vars/ shared library

        gcpCloudBuild(args: '--project="$GCR_PROJECT" --substitutions="_REGISTRY=$GCR_REGISTRY,_IMAGE_VERSION=$GIT_COMMIT,_GIT_BRANCH=${GIT_BRANCH##*/}"',
                      timeoutMinutes: 90,
                      skipIfDockerImagesExist: env.DOCKER_IMAGES.tokenize(',').collect { "$it:$VERSION" }
                      )

        timeout(time: 60, unit: 'MINUTES') {
          sh 'make'
          // or
          sh './gcp_ci_build.sh'  // script in https://github.com/HariSekhon/DevOps-Bash-tools

          //retry(3) {
          //  //sh 'apt update -q'
          //  //sh 'apt install -qy make'
          //  //sh 'make init'
          //  sh """
          //    setup/ci_bootstrap.sh &&
          //    make init
          //  """
          //}
        }

        //timeout(time: 180, unit: 'MINUTES') {
        //  sh 'make ci'
        //}

        // saves artifacts to Jenkins master for basic reporting and archival - not a substitute for Nexus / Artifactory
        // archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
      }
    }

    // ========================================================================== //
    //                         P o s t - B u i l d   H o o k
    // ========================================================================== //

    stage('Post-Build Hook') {
      when {
        expression {
          fileExists 'jenkins/postBuildHook.sh'
        }
      }
      steps {
        sh 'jenkins/postBuildHook.sh'
      }
    }

    // ========================================================================== //
    // Artifactory - consider using the built-in artifact publishing support in your build tool (Maven/Gradle etc),
    //               but jfrog-cli is here if your needs are more complex
    stage('Artifactory Upload'){
      steps {
        container('jfrog-cli'){
          sh 'jfrog rt upload --url "$ARTIFACTORY_URL" --access-token "$ARTIFACTORY_ACCESS_TOKEN" target/myapp-0.0.1-SNAPSHOT.jar myapp/'
        }
      }
    }

    // ========================================================================== //
    //                         D o c k e r   B u i l d s
    // ========================================================================== //

    // GitHub Container Registry
    stage('GHCR Login') {
      agent { label 'docker-builder' }
      steps {
        milestone(ordinal: null, label: "Milestone: GHCR Login")
        timeout(time: 1, unit: 'MINUTES') {
          sh """#!/usr/bin/env bash
            docker login ghcr.io -u '$GITHUB_TOKEN_USR' --password-stdin <<< '$GITHUB_TOKEN_PSW'
          """
        }
      }
    }
    stage('Docker Build') {
      agent { label 'docker-builder' }
      //when {
      //  beforeAgent true
      //  changeset 'Dockerfile' // if the docker image doesn't ADD/COPY anything from local repo then only rebuild it when the Dockerfile changes
      //}
      steps {
        milestone(ordinal: null, label: "Milestone: Docker Build")
        timeout(time: 60, unit: 'MINUTES') {
          // check 'DOCKER_BUILDKIT = 1' is set in environment {} section
          sh "docker build -t '$DOCKER_IMAGE':'$DOCKER_TAG' --build-arg=BUILDKIT_INLINE_CACHE=1 --cache-from '$DOCKER_IMAGE':'$DOCKER_TAG' ."
        }
      }
    }

    // ========================================================================== //
    //            Container Vulnerability Scanning before Docker Push
    // ========================================================================== //

    // ========================================================================== //
    //                                   T r i v y
    // ========================================================================== //

    stage('Trivy') {
      steps {
        milestone(ordinal: null, label: "Milestone: Trivy")
        // need to set buildResult to 'SUCCESS' to prevent it setting it to 'FAILURE' - otherwise the pipeline will continue but even though the deployment happens it'll mark it as failed
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
          // funcs in vars/ shared library
          trivy('fs . ')
          trivy("image --no-progress --timeout 20m --exit-code 1 $DOCKER_IMAGE:$DOCKER_TAG")
          // iterates a given list list of docker images and calls trivy('image...') for each one
          trivyScanDockerImages(["docker_image1:tag1", "docker_image2:tag2"])
          //trivyScanDockerImages(env.DOCKER_IMAGES_TAGS.split(',') as List)
        }
      }
    }

    // ========================================================================== //
    //                                   G r y p e
    // ========================================================================== //

    stage('Grype') {
      steps {
        milestone(ordinal: null, label: "Milestone: Grype")
        // need to set buildResult to 'SUCCESS' to prevent it setting it to 'FAILURE' - otherwise the pipeline will continue but even though the deployment happens it'll mark it as failed
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
          // func in vars/ shared library
          grype("dir:.") // for locally built packages
          grype("$DOCKER_IMAGE:$DOCKER_TAG")
          //grype(["docker_image1:tag1", "docker_image2:tag2"])
          //grype(env.DOCKER_IMAGES_TAGS.split(',') as List)
        }
      }
    }

    // ========================================================================== //
    //                  D o c k e r   P u s h   A f t e r   S c a n
    // ========================================================================== //

    stage('Docker Push') {
      agent { label 'docker-builder' }
      steps {
        milestone(ordinal: null, label: "Milestone: Docker Push")
        timeout(time: 15, unit: 'MINUTES') {
          sh "docker push '$DOCKER_IMAGE':'$DOCKER_TAG'"
        }
      }
    }

    // Jenkins Deploys are further down after Approval

  // ========================================================================== //
  //           T e r r a f o r m   /   T e r r a g r u n t   S t a g e s
  // ========================================================================== //

  // Terraform / Terragrunt Apply is further down after Approval

    stage('Terraform Init') {
      steps {
        terraformInit()  // func in vars/ shared library
      }
    }

    stage('Terragrunt Init') {
      steps {
        terragruntInit()  // func in vars/ shared library
      }
    }

    stage('tfsec') {
      steps {
        tfsec()  // func in vars/ shared library
      }
    }

    stage('Terraform Plan') {
      steps {
        terraformPlan()  // func in vars/ shared library
      }
    }

    stage('Terragrunt Plan') {
      steps {
        terragruntPlan()  // func in vars/ shared library
      }
    }

  // ========================================================================== //
  //                             L i q u i b a s e
  // ========================================================================== //

  // Liquibase Update is further down after Approval

    stage('Liquibase Status'){
      steps {
        liquibaseStatus()  // func in vars/ shared library
      }
    }

  // ========================================================================== //
  //                              A p p r o v a l
  // ========================================================================== //

    // Should apply to any production release or Terraform / Terragrunt apply for safety

    stage('Approval') {
      // by default input applies after options{} but before agent{} or when{}
      when {
        beforeAgent true  // don't spin up a K8s pod if we don't need to execute
        // TODO: test with and without
        // https://www.jenkins.io/doc/book/pipeline/syntax/#evaluating-when-before-the-input-directive
        beforeInput true  // change order to evaluate when{} first to only prompt if this is on production branch
        branch '*/production'
        //branch pattern: '^.*/(main|master|production)$', comparator: 'REGEXP' }
      }
      steps {
        approval(
          // all these settings are optional
          submitter: "$DEPLOYERS", // you can set this env var at the global Jenkins level, see vars/approval.groovy for details
          ok: 'Deploy',            // set the Ok button to say Deploy instead
          timeout: 2,              // default: 60 (mins), see vars/approval.groovy
          timeoutUnits: 'HOURS'    // default: MINUTES, see vars/approval.groovy
        )

//        // https://www.jenkins.io/doc/book/pipeline/syntax/#input
//        //input "Proceed to deployment?"
//        timeout(time: 1, unit: 'HOURS') {
//          input (
//            message: '''Are you sure you want to release this build to production?
//
//This prompt will time out after 1 hour''',
//            ok: "Deploy",
//            // Azure AD security group is referenced by just name, whereas Microsoft 365 email distribution group is referenced by email address
//            submitter: "platform-engineering",  // only allow users in platform engineering group to Approve. Warning: users outside this group can still hit Abort!
//            // only do this if you have defined parameters and need to choose which property to store the result in
//            //submitterParameter: "SUBMITTER"
//          )
//        }
      }
    }

  // ========================================================================== //
  //           Terraform / Terragrunt Apply or Production Deployments
  // ========================================================================== //

    stage('Terraform Apply') {
      //when {
      //  beforeAgent true  // don't spin up a K8s pod if we don't need to execute
      //  branch pattern: '^.*/(main|master|production)$', comparator: 'REGEXP'
      //}
      steps {
        terraformApply()  // func in vars/ shared library
      }
    }

    stage('Terragrunt Apply') {
      //when {
      //  beforeAgent true  // don't spin up a K8s pod if we don't need to execute
      //  branch pattern: '^.*/(main|master|production)$', comparator: 'REGEXP'
      //}
      steps {
        terragruntApply()  // func in vars/ shared library
      }
    }

  // ========================================================================== //
  //                             L i q u i b a s e
  // ========================================================================== //

    stage('Liquibase Update'){
      steps {
        liquibaseUpdate()  // func in vars/ shared library
      }
    }

  // ========================================================================== //
  //                               D e p l o y s
  // ========================================================================== //

  // Deploys are intentionally below Approval

    // no longer needed if pulling git-kustomize docker image in jenkins-pod.yaml
    //stage('Download Kustomize') {
    //  steps {
    //    downloadKustomize()  // func in vars/ shared library
    //  }
    //}

    // ArgoCD GitOps Deployment
    stage('ArgoCD Deploy') {
      // XXX: lock to serialize GitOps K8s image update and ArgoCD deployment to ensure accurate deployment rollout status for each build before allowing another Git change, otherwise ArgoCD could quickly release the newer change, masking a breakage in a previous build not rolling out properly
      steps {
        lock(resource: "ArgoCD Deploy - App: $APP, Environment: $ENVIRONMENT", inversePrecedence: true) {
          // forbids older deploys from starting
          milestone(ordinal: null, label: "Milestone: ArgoCD Deploy")

          container('git-kustomize') {
            // credential needs to match the ID field, not the name, otherwise it'll fail with "FATAL: [ssh-agent] Could not find specified credentials" but continue with a blank ssh agent loaded in the environment causing SSH / Git clone failures later on
            // ignoreMissing: false (default) doesn't work and there is no issue tracker on the github project page to report this :-/
            sshagent (credentials: ['my-ssh-key'], ignoreMissing: false) {
              sshKnownHostsGitHub()
              gitKustomizeImage(dockerImages: env.DOCKER_IMAGES.tokenize(','),
                                repo: "$GITOPS_REPO",
                                branch: "$ENVIRONMENT",
                                dir: "$APP/$ENVIRONMENT",
                                version: "$VERSION")
            }
          }

          argoDeploy("$APP-$ENVIRONMENT")  // func in vars/ shared library
        }
      }
    }

    stage('Deploy') {
      //when { branch pattern: '^.*/production$', comparator: 'REGEXP' }
      //when { branch pattern: '*/production' }
      //when { branch '*/production' }
      //when { environment name: 'DEPLOY_TO', value: 'production' }
      //when { allOf { branch 'master'; environment name: 'DEPLOY_TO', value: 'production' } }
      //when {
      //  beforeAgent true  // don't spin up a K8s pod if we don't need to execute
      //  branch pattern: '^.*/(main|master|production)$', comparator: 'REGEXP'
      //}

      // prompt to deploy - use in separate stage Approval instead
      //input "Deploy?"

      // XXX: move to Shared Libary to use Groovy to define lock in a String and add an informational Acquiring Lock message to make it more obvious when a build is waiting on a lock before progressing, otherwise they just look like they're hanging
      //
      // see vars/ directory in this repo
      //
      // String deploymentLock = "Deploy - App: $APP, Environment: $ENVIRONMENT"
      // echo "Acquiring Deployment Lock: $deploymentLock"
      // lock(resource: deploymentLock, inversePrecedence: true) {

      // discard other deploys once this one has been chosen
      // use Lockable Resources plugin to limit deploy concurrency to 1
      // inversePrecedence: true makes Jenkins use the most recent deployment first, which when combined with Milestone, discards older deploys
      steps {
        lock(resource: "Deploy - App: $APP, Environment: $ENVIRONMENT", inversePrecedence: true) {
          // forbids older deploys from starting
          milestone(ordinal: null, label: "Milestone: Deploy")
          echo 'Deploying...'
          // push artifacts and/or deploy to production
          timeout(time: 15, unit: 'MINUTES') {
            sh 'make deploy'
            // OR
            // - this autoloads kubeconfig from GKE using GCP serviceaccount credential key
            sh './gcp_ci_deploy_k8s.sh'  // https://github.com/HariSekhon/DevOps-Bash-tools
            // OR
            argoDeploy()  // func in vars/ shared library
          }
        }
      }
    }

    stage('Deploy Canary') {
      // XXX: remember to escape backslashes (double backslash)
      //when { branch pattern: '^*/staging$', comparator: 'REGEXP' }
      when { branch '*/staging' }
      //when {
      //  beforeAgent true  // don't spin up a K8s pod if we don't need to execute
      //  branch pattern: '^.*/(main|master|production)$', comparator: 'REGEXP'
      //}

      steps {
        echo 'Deploying Canary release...'
        // uses a Jenkins credential containing an uploaded .kube/config
        withKubeConfig([credentialsId:kubeconfig, contextName:canary]){
          sh 'kubectl apply -f manifests/'
        }
      }
    }

    stage('Deploy Production') {
      // protection for Multibranch Pipelines to not deploy the wrong environment (prod may have side effects eg. customer notifications that you absolutely cannot allow into Staging / Development environments)
      // XXX: remember to escape backslashes (double backslash)
      //when { branch pattern: '^.*/production$', comparator: 'REGEXP' }
      // path glob by default
      when { branch '*/production' }
      //when {
      //  beforeAgent true  // don't spin up a K8s pod if we don't need to execute
      //  branch pattern: '^.*/(main|master|production)$', comparator: 'REGEXP'
      //}

      steps {
        echo 'Deploying Production release...'
        // EITHER
        withKubeConfig([credentialsId:kubeconfig, contextName:prod]){
          sh 'kubectl apply -f manifests/'
        }
        // OR - using external scripts ties this to the source repo
        sh 'path/to/gcp_ci_deploy_k8s.sh'  // https://github.com/HariSekhon/DevOps-Bash-tools
        // OR
        argoDeploy()  // func in vars/ shared library
      }
    }

    stage('Cloudflare Cache Purge') {
      steps {
        CloudflarePurgeCache()  // func in vars/ shared library
      }
    }

    // see https://jenkins.io/blog/2017/09/25/declarative-1/
    stage('Parallel stuff') {
      parallel {
        stage('stage 1') {
          steps {
            sh '...'
          }
        }
        stage('stage B') {
          steps {
            sh '...'
          }
        }
      }
    }

    // release entire environment by triggering all the Pipelines for the apps in that environment
    stage("Trigger Deployments"){
      parallel {
        stage('Trigger WWW Deployment'){
          steps {
            build (
              job: "WWW $ENVIRONMENT",
              wait: false
            )
          }
        }
        stage('Trigger APP1 Deployment'){
          steps {
            build (
              job: "APP1 $ENVIRONMENT",
              wait: false
            )
          }
        }
        stage('Trigger APP2 Deployment'){
          steps {
            build (
              job: "APP2 $ENVIRONMENT",
              wait: false
            )
          }
        }
      }
    }

  }

  // ========================================================================== //
  //                                    P o s t
  // ========================================================================== //

  // https://www.jenkins.io/doc/book/pipeline/syntax/#post
  post {
    always {
      echo 'Always'
      //deleteDir()  // clean up workspace - not needed if you're running each build in a separate Docker container or Kubernetes pod

//      // collect JUnit reports for Jenkins UI
//      junit 'build/reports/**/*.xml'
//      junit '**/target/*.xml'
//
//      // collect artifacts to Jenkins for analysis
//      archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
//      archiveArtifacts 'src/*/*/*.tap'
//      step([$class: "TapPublisher", testResults: 'src/*/*/*.tap', verbose: false])

//      script {
//        sh 'chmod -R o+w target/allure-reports'  // if build runs as root but Jenkins Allure plugin runs as jenkins user
//        //sh 'ls -lR target/allure-reports'        // check the file perms, 0022 umask by default should be ok
//        allure([
//          includeProperties: false,
//          jdk: '',
//          properties: [],
//          reportBuildPolicy: 'ALWAYS',
//          results: [[path: 'target/allure-results']]
//        ])
//        // fancier notification in Slack channel full breakdown than the one liners below, slack-properties.json needs to contain '{ "app": { "bot": { "token": "...", "chat": "#jenkins-alerts-qa", ... } } }'
//        sh 'java  -DprojectName="$JOB_NAME" -Dconfig.file=slack-properties.json -Denv=staging.mycompany.co.uk -DreportLink="$BUILD_URL" -jar allure-notifications-3.1.1.jar'
//        sh 'docker logout'
//      }

    }
    success {
      echo 'SUCCESS!'
    }
    fixed {
      echo "FIXED!"
      Notify()
    }
    failure {
      echo 'FAILURE!'
      Notify()  // calls slackNotify or other notification functions
      // or
      slackNotify() // resolves and tags committers who broke build

      //mail to: team@example.com, subject: "Jenkins Pipeline Failed - Job '${env.JOB_NAME}' Build ${env.BUILD_NUMBER}"
    }
    //unsuccessful {
    //}
    unstable {
      echo 'UNSTABLE!'
    }
    // only runs if status changed from last run
    changed {
      echo 'Pipeline state change! (success vs failure)'
    }
    //cleanup {
    //}
  }
}

// https://github.com/jenkinsci/pipeline-examples/tree/master/jenkinsfile-examples/sonarqube:
//
// - Manage Jenkins > Configure System and set up SonarQube Servers
// - Create a new Pipeline job and set the url for your SCM, which contains the Jenkinsfile
//
//node {
//  stage 'Checkout'
//
//  // default checkout of the repo containing this Jenkinsfile - this is done automatically for you in Declarative Pipelines
//  checkout scm
//
//  stage 'Gradle Static Analysis'
//  withSonarQubeEnv {
//    sh "./gradlew clean sonarqube"
//  }
//}
