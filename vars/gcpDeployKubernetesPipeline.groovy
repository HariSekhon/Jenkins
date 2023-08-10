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
//                   GCP Cloud Build & Deploy to Kubernetes
// ========================================================================== //

// Templated pipeline:
//
// - builds docker images in GCP Cloud Build which publishes to GCR
//   - or if CloudBuild is triggered externally from GitHub, we set no_cloudbuild: true and just wait for the images to appear in GCR
// - updates the GitOps Kubernetes repo given directory via Kustomize with the version (or git hashref if no version given)
// - publishes the new docker images to Kubernetes via triggering their corresponding ArgoCD app
// - waits for ArgoCD sync and health checks
// - purges Cloudflare cache

// Extras (they don't fail the pipeline or block deployment):
//
// - Scans the Git repo using:
//   - Grype (results in pipeline)
//   - Trivy (results in pipeline)
//   - SonarQube (results in $SONAR_HOST_URL server)
//
// - Scans the created docker images using:
//   - Clair
//   - Grype
//   - Trivy

// Important shared environment variables that should be defined on the Jenkins server:
//
// - ARGOCD_SERVER      eg. argocd.domain.com  - without the https:// ingress prefix
// - DOCKER_HOST        eg. tcp://docker.docker.svc.cluster.local:2375  - for Docker Pull to cache images before calling Grype and Trivy
// - GITOPS_REPO        eg. git@github.com:<org>/<repo>
// - GITOPS_BRANCH      eg. master
// - GCR_PROJECT        eg. shared-project  - where the docker images are built in CloudBuild and stored in GCR
// - NO_CODE_SCAN       eg. optional. Set to 'true' to code scan stage and related downloads
// - NO_CONTAINER_SCAN  eg. optional. Set to 'true' to container scan stage and related downloads
// - TRIVY_SERVER       eg. http://trivy.trivy.svc.cluster.local:4954
// - TRIVY_DEBUG        eg. set to 'true' if you want better trivy logging

def call (Map args = [
                        project: '',  // GCP project id to run commands against (except for CloudBuild which is always run in --project "$GCR_PROJECT" environment variable to share the same docker images from a shared build project unless overridden by the cloudbuild arg
                        region: '',   // GCP compute region
                        app: '',      // App name - used by ArgoCD
                        version: '',  // tags docker images with this, or $GIT_COMMIT if version is not defined
                        env: '',      // Environment, eg, 'uk-dev', 'us-staging' etc.. - suffixed to ArgoCD app name calls and used if k8s_dir not defined
                        env_vars: [:],  // a Map of environment variables and their values to load to the pipeline
                        creds: [:],     // a Map of environment variable keys and credentials IDs to populate each one with
                        container: 'gcloud-sdk',  // the container to run the build in
                        yamlFile: 'ci/jenkins-pod.yaml',  // the Kubernetes agent yaml file path
                        cloudbuild_args: '', // GCP CloudBuild args if needing to customize eg. to pass different or additional environment variables to the build
                        cloudbuild_config: 'cloudbuild.yaml',  // CloudBuild config file to use (will be ignored if cloudbuild_args is supplied but does not reference the $CLOUDBUILD_CONFIG environment variable)
                        no_cloudbuild: false,  // set to 'true' to not run CloudBuild but instead wait for the docker image tags appear in GCR from externally triggered CloudBuild or other image build process
                        gcp_serviceaccount_key: '',  // the Jenkins secret credential id of the GCP service account auth key
                        gcr_registry: '',  // eg. 'eu.gcr.io' or 'us.gcr.io'
                        images: [],   // List of docker image names (not prefixed by GCR/GAR registries) to test for existence to skip CloudBuild if all are present
                        k8s_dir: '',  // the Kubernetes GitOps repo's directory to Kustomize the image tags in before triggering ArgoCD
                        no_code_scan: false,  // set to 'true' or environment variable NO_CODE_SCAN=true to not run Code Scanning stage - tip: set this at the Jenkins global server to disable for all pipelines templated from this template
                        no_container_scan: false,  // set to 'true' or environment variable NO_CONTAINER_SCAN=true to not run Container Scanning stage - tip: set this at the Jenkins global server to disable for all pipelines templated from this template
                        approval_required: false,
                        approvers: '',
                        cloudflare_email: '',  // if both cloudflare email and zone id are set causes a Cloudflare Cache Purge at the end of the pipeline
                        cloudflare_zone_id: '',
                        timeoutMinutes: 60,  // total pipeline timeout limit to catch stuck pipelines
                      ]
      ) {

  //String container = args.container ?: error('you must specify a container and not execute in the jnlp default container as that will almost certainly fail for lack of tools and permissions')
  String container = args.container ?: 'gcloud-sdk'
  // yamlFile is an arg to agent{ kubernetes {} } so choose a different variable name
  String yamlFilePath = args.yamlFile ?: 'ci/jenkins-pod.yaml'
  int timeoutMins = args.timeoutMinutes ?: 60

  pipeline {

    agent {
      kubernetes {
        defaultContainer container
        yamlFile yamlFilePath
      }
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
    }

    options {
      ansiColor('xterm')
      buildDiscarder(logRotator(numToKeepStr: '30'))
      timestamps()
      timeout (time: timeoutMins, unit: 'MINUTES')
    }

    environment {
      APP         = "${ args.app ?: env.APP ?: error('app arg not specified and APP environment variable not already set') }"
      ENVIRONMENT = "${ args.env ?: env.ENVIRONMENT ?: env.ENV ?: error('env arg not specified and ENVIRONMENT/ENV environment variables not already set')}"

      CLOUDSDK_CORE_PROJECT   = "${ args.project ?: env.CLOUDSDK_CORE_PROJECT   ?: error('project arg not specified and CLOUDSDK_CORE_PROJECT environment variable not already set') }"
      CLOUDSDK_COMPUTE_REGION = "${ args.region  ?: env.CLOUDSDK_COMPUTE_REGION ?: error('region arg not specified and CLOUDSDK_COMPUTE_REGION environment variable not already set') }"

      CLOUDBUILD_CONFIG = "${ args.cloudbuild_config ?: 'cloudbuild.yaml' }"

      GCP_SERVICEACCOUNT_KEY = credentials("${ args.gcp_serviceaccount_key ?: env.GCP_SERVICEACCOUNT_KEY ?: error('gcp_serviceaccount_key arg not specified and GCP_SERVICEACCOUNT_KEY environment variable not already set') }")

      GCR_REGISTRY = "${ args.gcr_registry ?: env.GCR_REGISTRY ?: error('gcr_registry arg not specified and GCR_REGISTRY environment variable not already set') }"

      ARGOCD_AUTH_TOKEN  = credentials('argocd-auth-token')
      CLOUDFLARE_API_KEY = credentials('cloudflare-api-key')
      GITHUB_TOKEN       = credentials('github-token')

      APPROVERS = "${ args.approvers ?: env.APPROVERS ?: error('approvers arg not specified and APPROVERS environment variable not already set') }"

      CLOUDFLARE_EMAIL   = "${ args.cloudflare_email ?: '' }"
      CLOUDFLARE_ZONE_ID = "${ args.cloudflare_zone_id ?: '' }"
    }

    stages {

      stage('Setup') {
        steps {
          script {
            env.VERSION = "${ args.version ?: env.GIT_COMMIT ?: error('version arg not specified and neither VERSION nor GIT_COMMIT environment variables are already set') }"  // CloudBuild tags docker images with this $VERSION variable
            env.K8S_DIR = "${ args.k8s_dir ?: env.K8S_DIR ?: "$APP/$ENVIRONMENT" }"  // Directory path in the GitOps Kubernetes repo in which to Kustomize edit the docker image tag versions
            env.NO_CODE_SCAN = args.no_code_scan ?: env.NO_CODE_SCAN ?: false
            env.NO_CONTAINER_SCAN = args.no_container_scan ?: env.NO_CONTAINER_SCAN ?: false
            if ( args.approval_required != null ) {
              env.APPROVAL_REQUIRED = "${ args.approval_required || false }"  // any value other than 'false' becomes 'true' explicitly this way so it's easier to see what the behaviour will be in printEnv()
              echo "Approval required set = '$APPROVAL_REQUIRED'"
            } else {
              env.APPROVAL_REQUIRED = false
              // in boolean context =~ works as expected rather than returning a matcher, don't use ==~ as it is anchored and breaks matching without surrounding .*
              if ( env.APPROVERS && env.ENVIRONMENT =~ /prod$|production/ ) {
                env.APPROVAL_REQUIRED = true
              }
              echo "Approval required inferred to be '$APPROVAL_REQUIRED'"
            }
          }
          loadEnvVars(args.env_vars)
          loadCredentials(args.creds)
          gcrGenerateEnvVarDockerImages(args.images)
          gitCommitShort()
          printEnv()
          catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            dockerInfo()
          }
        }
      }

      stage('Downloads') {
        parallel {

          stage('Download ArgoCD') {
            steps {
              downloadArgo()
            }
          }

          stage('Download Clairctl') {
            when {
              expression { ! "${env.NO_CONTAINER_SCAN}".toBoolean() }
            }
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                downloadClairctl()
              }
            }
          }

          // Jenkins plugin doesn't expose all grype switches in grypeScanner function so download and call with full settings:
          //
          //    https://plugins.jenkins.io/grypescanner/
          //
          stage('Download Grype') {
            when {
              expression { ! ( "${env.NO_CODE_SCAN}".toBoolean() && "${env.NO_CONTAINER_SCAN}".toBoolean() ) }
            }
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                downloadGrype()
              }
            }
          }

          stage('Download Kustomize') {
            steps {
              downloadKustomize()
            }
          }

          stage('Download Trivy') {
            when {
              expression { ! ( "${env.NO_CODE_SCAN}".toBoolean() && "${env.NO_CONTAINER_SCAN}".toBoolean() ) }
            }
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                downloadTrivy()
              }
            }
          }

          stage('Install NodeJS') {
            when {
              expression { ! "${env.NO_CODE_SCAN}".toBoolean() }
            }
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                // for sonar scanner to avoid this:
                // org.sonar.plugins.javascript.nodejs.NodeCommandException: Error when running: 'node -v'. Is Node.js available during analysis?
                installPackages(['nodejs'])
              }
            }
          }

        }
      }

      stage('Code Scanning') {
        when {
          expression { ! "${env.NO_CODE_SCAN}".toBoolean() }
        }
        parallel {

          stage('Grype') {
            steps {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                grypeFS()
              }
            }
          }

          stage('SonarQube') {
            steps {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                sonarScanner()
              }
            }
          }

          stage('Trivy') {
            steps {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                trivyFS()
              }
            }
          }

        }
      }

      stage('GCP Auth') {
        steps {
          gcpActivateServiceAccount()
          printAuth()
        }
      }

      stage('GCP Docker Auth') {
        when {
          expression { ! "${env.NO_CONTAINER_SCAN}".toBoolean() }
        }
        steps {
          gcpDockerAuth()
        }
      }

      stage('GCP CloudBuild') {
        when {
          expression { ! "${args.no_cloudbuild}".toBoolean() }
        }
        steps {
          gcpCloudBuild(args: args.cloudbuild_args ?: '--config="$CLOUDBUILD_CONFIG" --project="$GCR_PROJECT" --substitutions="_REGISTRY=$GCR_REGISTRY,_IMAGE_VERSION=$VERSION,_GIT_BRANCH=${GIT_BRANCH##*/}"',
                        timeoutMinutes: timeoutMins,
                        // auto-inferred now
                        //skipIfDockerImagesExist: env.DOCKER_IMAGES.split(',').collect { "$it:$VERSION" }
                        )
        }
      }

      stage('Wait for GCR Docker Images') {
        // used to do this only for externally triggered CloudBuild but actually this is useful to check the list of docker images provided
        // are actually what we think they are rather than just waiting for ArgoCD to deploy and then realizing ImagePullBackoff later
        //when {
        //  expression { "${args.no_cloudbuild}".toBoolean() }
        //}
        steps {
          gcrDockerImagesExistWait()
        }
      }

      stage('Docker') {
        parallel {
          stage('Add GCR Docker Image Tags') {
            steps {
              // XXX: needed because we use short git commits for deployments if a VERSION isn't specified
              gcrTagGitCommitShort()
            }
          }

          stage('Docker Pull') {
            when {
              expression { ! "${env.NO_CONTAINER_SCAN}".toBoolean() }
            }
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                // pre-load cache for Grype and Trivy
                dockerPull()
              }
            }
          }

        }
      }

      stage('Container Image Scanning') {
        when {
          expression { ! "${env.NO_CONTAINER_SCAN}".toBoolean() }
        }
        parallel {
          // being extremely slow, and hit this bug giving no useful results:
          //
          //    https://github.com/quay/clair/issues/1756
          //
          stage('Clair') {
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                clair()
              }
            }
          }
          stage('Grype') {
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                grype()
              }
            }
          }
          stage('Trivy') {
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                trivyImages()
              }
            }
          }
        }
      }

      stage('Approval') {
        when {
          expression { "${env.APPROVAL_REQUIRED}".toBoolean() }
        }
        steps {
          approval(submitter: "$APPROVERS", timeout: 24, timeoutUnits: 'HOURS')
        }
      }

      stage('ArgoCD Deploy') {
        steps {
          lock(resource: "ArgoCD Deploy - App: $APP, Environment: $ENVIRONMENT", inversePrecedence: true) {
            milestone(ordinal: null, label: "Milestone: ArgoCD Deploy")
            sshagent (credentials: ['github-ssh-key'], ignoreMissing: false) {
              sshKnownHostsGitHub()
              gitKustomizeImage()
            }
            argoDeploy("$APP-$ENVIRONMENT")
          }
        }
      }

      stage('Cloudflare Cache Purge') {
        steps {
          script {
            if (env.CLOUDFLARE_EMAIL && env.CLOUDFLARE_ZONE_ID) {
              cloudflarePurgeCache()
            }
          }
        }
      }

    }

    post {
      failure {
        Notify()
      }
      fixed {
        Notify()
      }
    }

  }

}
