@Library('github.com/harisekhon/jenkins@master') _

pipeline {
  agent any

  options {
    timestamps()
    timeout(time: 2, unit: 'HOURS')
    parallelsAlwaysFailFast()
    rateLimitBuilds(throttle: [count: 3, durationName: 'minute', userBoost: false])
    buildDiscarder(logRotator(numToKeepStr: '100'))
    ansiColor('xterm')
  }

  triggers {
    pollSCM('H/10 * * * *')
    cron('H 10 * * 1-5')
  }

  environment {
    ENV = 'dev'
    APP = 'www'
    VERSION = "$GIT_COMMIT"
    GITOPS_REPO = 'git@github.com/HariSekhon/app-k8s'
    CI = true
    GIT_USERNAME = 'Jenkins'
    GIT_EMAIL = 'platform-engineering@MYCOMPANY.CO.UK'
    GITHUB_SSH_KNOWN_HOSTS = credentials('github-ssh-known-hosts')
    GITLAB_SSH_KNOWN_HOSTS = credentials('gitlab-ssh-known-hosts')
    AZURE_DEVOPS_SSH_KNOWN_HOSTS = credentials('azure-devops-ssh-known-hosts')
    BITBUCKET_SSH_KNOWN_HOSTS = credentials('bitbucket-ssh-known-hosts')
    SSH_KNOWN_HOSTS = """
      $GITHUB_SSH_KNOWN_HOSTS
      $GITLAB_SSH_KNOWN_HOSTS
      $AZURE_DEVOPS_SSH_KNOWN_HOSTS
      $BITBUCKET_SSH_KNOWN_HOSTS
    """
    AWS_ACCESS_KEY_ID = credentials('aws-secret-key-id')
    AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
    GCP_SERVICEACCOUNT_KEY = credentials('gcp-serviceaccount-key')
    GOOGLE_APPLICATION_CREDENTIALS = "$WORKSPACE_TMP/.gcloud/application-credentials.json.$BUILD_TAG"
    DIGITALOCEAN_ACCESS_TOKEN = credentials('digitalocean-access-token')
    GITHUB_TOKEN = credentials('github-token')
    AWS_ACCESS_KEY = credentials('aws')
    AWS_ACCOUNT_ID = credentials('aws-account-id')
    AWS_DEFAULT_REGION = 'eu-west-2'
    AWS_ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
    CLOUDSDK_CORE_PROJECT = 'mycompany-dev'
    CLOUDSDK_COMPUTE_REGION = 'europe-west2'
    GCR_REGISTRY = 'eu.gcr.io'
    CLOUDFLARE_API_KEY = credentials('cloudflare-api-key')
    DOCKER_BUILDKIT = 1
    DOCKER_IMAGE = "$AWS_ECR_REGISTRY/$APP"
    DOCKER_TAG = "$GIT_COMMIT"
    ARTIFACTORY_URL = 'http://x.x.x.x:8082/artifactory/'
    ARTIFACTORY_ACCESS_TOKEN = credentials('artifactory-access-token')
    ARGOCD_SERVER = 'argocd.domain.com'
    ARGOCD_AUTH_TOKEN = credentials('argocd-auth-token')
    HERMIT_ENV_VARS = sh(returnStdout: true, script: './bin/hermit env --raw').trim()
    THREAD_COUNT = 6
    SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    SEMGREP_BASELINE_REF = "origin/${env.CHANGE_TARGET}"
    SEMGREP_TIMEOUT = "300"
  }

  stages {
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
          ]
        )
      }
    }

    stage('Git Merge') {
      when {
        beforeAgent true
        branch '*/staging'
      }
      steps {
        echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
        timeout(time: 1, unit: 'MINUTES') {
          sh script: 'whoami', label: 'User'
          sh script: 'id', label: 'id'
          sh script: 'env | sort', label: 'Environment'
        }
        lock(resource: 'Git Merge Staging to Dev', inversePrecedence: true) {
          milestone ordinal: null, label: "Milestone: Git Merge"
          timeout(time: 5, unit: 'MINUTES') {
            sshagent (credentials: ['jenkins-ssh-key-for-github']) {
              retry(2) {
                sh 'path/to/git_merge_branch.sh staging dev'
              }
            }
          }
        }
      }
    }

    stage('Setup') {
      steps {
        milestone(ordinal: null, label: "Milestone: Setup")
        label 'Setup'
        container('jq') {
          sh "wget -qO- ifconfig.co/json | jq -r '.ip'"
          sh 'script_using_jq.sh'
        }

        script {
          currentBuild.displayName = "$BUILD_DISPLAY_NAME (${GIT_COMMIT.take(8)})"
          workspace = "$env.WORKSPACE"
        }

        sh '''
          for x in /etc/mybuild.d/*.sh; do
            if [ -r "$x" ]; then
              source $x;
            fi;
          done;
        '''

        script {
          env.DOCKER_IMAGES = [
            "$APP-php",
            "$APP-nginx",
            "$APP-cache",
            "$APP-sql-proxy",
          ].collect{"$GCR_REGISTRY/$GCR_PROJECT/$it"}.join(',')
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
        withGroovy(tool: '3.0.9'){
          sh "groovy --version"
        }
      }
    }

    stage('Maven Clean') {
      steps {
        sh "./mvnw clean"
      }
    }

    stage('Wait for Selenium Grid to be up') {
      steps {
        sh script: "./selenium_hub_wait_ready.sh '$SELENIUM_HUB_URL' 60"
      }
    }

    stage('Test') {
      steps {
        milestone(ordinal: null, label: "Milestone: Test")
        echo 'Testing...'
        timeout(time: 60, unit: 'MINUTES') {
          sh 'make test'
        }
      }
    }

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

    stage('Run Single Package Tests') {
      steps {
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

    stage('Run Desktop Tests') {
      steps {
        catchError (buildResult: 'FAILURE', stageResult: 'FAILURE') {
          sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dgroups=com.mydomain.category.interfaces.DesktopTests -DthreadCount='$THREAD_COUNT'"
        }
      }
    }

    stage('Run Mobile Tests') {
      steps {
        sh "./mvnw test -DselenoidUrl='$SELENIUM_HUB_URL' -Dgroups=com.mydomain.category.interfaces.MobileTests -Dmobile=true -DthreadCount='$THREAD_COUNT'"
      }
    }

    stage('JFrog Artifactory') {
      steps {
        jf '-v'
        jf 'rt ping'
        jf 'rt u target/ my-repo/'
      }
    }

    stage('Surefire Report'){
      steps {
        sh './mvnw clean install site surefire-report:report'
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

    stage('SonarQube Scan'){
      steps {
        withSonarQubeEnv(installationName: 'mysonar'){
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

    stage('Checkov') {
      when {
        expression { env.CHANGE_ID && env.BRANCH_NAME.startsWith("PR-") }
        beforeAgent true
      }
      steps {
        checkov()
      }
    }

    stage('Semgrep') {
      when {
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

    stage('KICS') {
      steps {
        container('kics'){
          sh 'mkdir -p results'
          sh 'kics scan --ci --no-color -p ${WORKSPACE} --output-path results --ignore-on-exit results --report-formats "json,sarif,html"'
          archiveArtifacts(artifacts: 'results/*.html,results/*.sarif,results/*.json', fingerprint: true)
        }
      }
    }

    stage('MegaLinter'){
      steps {
        container('megalinter'){
          steps {
            sh '/entrypoint.sh'
          }
        }
      }
    }

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

    stage('Build') {
      environment {
        DEBUG = '1'
      }
      steps {
        milestone(ordinal: null, label: "Milestone: Build")
        echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
        echo 'Building...'
        cloudBuild(timeout_minutes=40)
        gcpCloudBuild(args: '--project="$GCR_PROJECT" --substitutions="_REGISTRY=$GCR_REGISTRY,_IMAGE_VERSION=$GIT_COMMIT,_GIT_BRANCH=${GIT_BRANCH##*/}"',
                      timeoutMinutes: 90,
                      skipIfDockerImagesExist: env.DOCKER_IMAGES.tokenize(',').collect { "$it:$VERSION" }
        )
        timeout(time: 60, unit: 'MINUTES') {
          sh 'make'
          sh './gcp_ci_build.sh'
        }
      }
    }

    stage('
 6 vulnerabilities detected
Message interrupted
Copilot was interrupted before it could finish this message.
Juan-Mendoza-NTTDATA/Jenkins
Jenkinsfile
puedes quitarle los comentarios? excepto los titulos que tienen el formato // ========================================================================== //
//                        J e n k i n s   P i p e l i n e
// ========================================================================== //
Claro, aquí tienes el Jenkinsfile con los comentarios eliminados, excepto los títulos formateados:

Groovy
@Library('github.com/harisekhon/jenkins@master') _

pipeline {

  // ========================================================================== //
  //                                  A g e n t s
  // ========================================================================== //

  agent any

  options {
    timestamps()
    timeout(time: 2, unit: 'HOURS')
    parallelsAlwaysFailFast()
    rateLimitBuilds(throttle: [count: 3, durationName: 'minute', userBoost: false])
    buildDiscarder(logRotator(numToKeepStr: '100'))
    ansiColor('xterm')
  }

  triggers {
    pollSCM('H/10 * * * *')
    cron('H 10 * * 1-5')
  }

  environment {
    ENV = 'dev'
    APP = 'www'
    VERSION = "$GIT_COMMIT"
    GITOPS_REPO = 'git@github.com/HariSekhon/app-k8s'
    CI = true
    GIT_USERNAME = 'Jenkins'
    GIT_EMAIL = 'platform-engineering@MYCOMPANY.CO.UK'
    GITHUB_SSH_KNOWN_HOSTS = credentials('github-ssh-known-hosts')
    GITLAB_SSH_KNOWN_HOSTS = credentials('gitlab-ssh-known-hosts')
    AZURE_DEVOPS_SSH_KNOWN_HOSTS = credentials('azure-devops-ssh-known-hosts')
    BITBUCKET_SSH_KNOWN_HOSTS = credentials('bitbucket-ssh-known-hosts')
    SSH_KNOWN_HOSTS = """
      $GITHUB_SSH_KNOWN_HOSTS
      $GITLAB_SSH_KNOWN_HOSTS
      $AZURE_DEVOPS_SSH_KNOWN_HOSTS
      $BITBUCKET_SSH_KNOWN_HOSTS
    """
    AWS_ACCESS_KEY_ID = credentials('aws-secret-key-id')
    AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
    GCP_SERVICEACCOUNT_KEY = credentials('gcp-serviceaccount-key')
    GOOGLE_APPLICATION_CREDENTIALS = "$WORKSPACE_TMP/.gcloud/application-credentials.json.$BUILD_TAG"
    DIGITALOCEAN_ACCESS_TOKEN = credentials('digitalocean-access-token')
    GITHUB_TOKEN = credentials('github-token')
    AWS_ACCESS_KEY = credentials('aws')
    AWS_ACCOUNT_ID = credentials('aws-account-id')
    AWS_DEFAULT_REGION = 'eu-west-2'
    AWS_ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
    CLOUDSDK_CORE_PROJECT = 'mycompany-dev'
    CLOUDSDK_COMPUTE_REGION = 'europe-west2'
    GCR_REGISTRY = 'eu.gcr.io'
    CLOUDFLARE_API_KEY = credentials('cloudflare-api-key')
    DOCKER_BUILDKIT = 1
    DOCKER_IMAGE = "$AWS_ECR_REGISTRY/$APP"
    DOCKER_TAG = "$GIT_COMMIT"
    ARTIFACTORY_URL = 'http://x.x.x.x:8082/artifactory/'
    ARTIFACTORY_ACCESS_TOKEN = credentials('artifactory-access-token')
    ARGOCD_SERVER = 'argocd.domain.com'
    ARGOCD_AUTH_TOKEN = credentials('argocd-auth-token')
    HERMIT_ENV_VARS = sh(returnStdout: true, script: './bin/hermit env --raw').trim()
    THREAD_COUNT = 6
    SLACK_MESSAGE = "Pipeline <${env.JOB_DISPLAY_URL}|${env.JOB_NAME}> - <${env.RUN_DISPLAY_URL}|Build #${env.BUILD_NUMBER}>"
    SEMGREP_BASELINE_REF = "origin/${env.CHANGE_TARGET}"
    SEMGREP_TIMEOUT = "300"
  }

  // ========================================================================== //
  //                                  S t a g e s
  // ========================================================================== //

  stages {

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
          ]
        )
      }
    }

    // ========================================================================== //
    //              G i t   M e r g e   B r a n c h   B a c k p o r t s
    // ========================================================================== //

    stage('Git Merge') {
      when {
        beforeAgent true
        branch '*/staging'
      }
      steps {
        echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
        timeout(time: 1, unit: 'MINUTES') {
          sh script: 'whoami', label: 'User'
          sh script: 'id', label: 'id'
          sh script: 'env | sort', label: 'Environment'
        }
        lock(resource: 'Git Merge Staging to Dev', inversePrecedence: true) {
          milestone ordinal: null, label: "Milestone: Git Merge"
          timeout(time: 5, unit: 'MINUTES') {
            sshagent (credentials: ['jenkins-ssh-key-for-github']) {
              retry(2) {
                sh 'path/to/git_merge_branch.sh staging dev'
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
        container('jq') {
          sh "wget -qO- ifconfig.co/json | jq -r '.ip'"
          sh 'script_using_jq.sh'
        }

        script {
          currentBuild.displayName = "$BUILD_DISPLAY_NAME (${GIT_COMMIT.take(8)})"
          workspace = "$env.WORKSPACE"
        }

        sh '''
          for x in /etc/mybuild.d/*.sh; do
            if [ -r "$x" ]; then
              source $x;
            fi;
          done;
        '''

        script {
          env.DOCKER_IMAGES = [
            "$APP-php",
            "$APP-nginx",
            "$APP-cache",
            "$APP-sql-proxy",
          ].collect{"$GCR_REGISTRY/$GCR_PROJECT/$it"}.join(',')
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
        withGroovy(tool: '3.0.9'){
          sh "groovy --version"
        }
      }
    }

    stage('Maven Clean') {
      steps {
        sh "./mvnw clean"
      }
    }

    // ========================================================================== //
    //                                    T e s t
    // ========================================================================== //

    stage('Wait for Selenium Grid to be up') {
      steps {
        sh script: "./selenium_hub_wait_ready.sh '$SELENIUM_HUB_URL' 60"
      }
    }

    stage('Test') {
      steps {
        milestone(ordinal: null, label: "Milestone: Test")
        echo 'Testing...'
        timeout(time: 60, unit: 'MINUTES') {
          sh 'make test'
        }
      }
    }

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

    stage('Run Single Package Tests') {
      steps {
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

    stage('Run Desktop Tests') {
      steps {
        catchError (buildResult: 'FAILURE', stageResult: 'FAILURE') {
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

    stage('JFrog Artifactory') {
      steps {
        jf '-v'
        jf 'rt ping'
        jf 'rt u target/ my-repo/'
      }
    }

    stage('Surefire Report'){
      steps {
        sh './mvnw clean install site surefire-report:report'
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
        withSonarQubeEnv(installationName: 'mysonar'){
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

    stage('Build') {
      environment {
        DEBUG = '1'
      }
      steps {
        milestone(ordinal: null, label: "Milestone: Build")
        echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
        echo 'Building...'
        cloudBuild(timeout_minutes=40)
        gcpCloudBuild(args: '--project="$GCR_PROJECT" --substitutions="_REGISTRY=$GCR_REGISTRY,_IMAGE_VERSION=$GIT_COMMIT,_GIT_BRANCH=${GIT_BRANCH##*/}"',
                      timeoutMinutes: 90,
                      skipIfDockerImagesExist: env.DOCKER_IMAGES.tokenize(',').collect { "$it:$VERSION" }
        )
        timeout(time: 60, unit: 'MINUTES') {
          sh 'make'
          sh './gcp_ci_build.sh'
        }
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
      steps {
        milestone(ordinal: null, label: "Milestone: Docker Build")
        timeout(time: 60, unit: 'MINUTES') {
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
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
          trivy('fs . ')
          trivy("image --no-progress --timeout 20m --exit-code 1 $DOCKER_IMAGE:$DOCKER_TAG")
          trivyScanDockerImages(["docker_image1:tag1", "docker_image2:tag2"])
        }
      }
    }

    // ========================================================================== //
    //                                   G r y p e
    // ========================================================================== //

    stage('Grype') {
      steps {
        milestone(ordinal: null, label: "Milestone: Grype")
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
          grype("dir:.")
          grype("$DOCKER_IMAGE:$DOCKER_TAG")
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

    // ========================================================================== //
    //           T e r r a f o r m   /   T e r r a g r u n t   S t a g e s
    // ========================================================================== //

    stage('Terraform Init') {
      steps {
        terraformInit()
      }
    }
  }
}
