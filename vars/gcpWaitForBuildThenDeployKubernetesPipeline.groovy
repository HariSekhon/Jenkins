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
//                GCP Wait for Build Then Deploy to Kubernetes
// ========================================================================== //

// Templated pipeline:
//
// - waits for docker images in GCR via externally triggered GCP Cloud Build
//   - docker images must be declared so we know what to test check for existence for
//   - only after all of those images become available will we proceed to deployment
// - updates the GitOps Kubernetes repo given directory via Kustomize with the version (or git hashref if no version given)
// - publishes the new docker images to Kubernetes via triggering their corresponding ArgoCD app
// - waits for ArgoCD sync and health checks

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
// - $ARGOCD_SERVER  eg. argocd.domain.com  - without the https:// ingress prefix
// - $DOCKER_HOST    eg. tcp://docker.docker.svc.cluster.local:2375  - for Docker Pull to cache images before calling Grype and Trivy
// - $GITOPS_REPO    eg. git@github.com:<org>/<repo>
// - $GITOPS_BRANCH  eg. master
// - $GCR_PROJECT    eg. shared-project  - where the docker images are built in CloudBuild and stored in GCR
// - $TRIVY_SERVER   eg. http://trivy.trivy.svc.cluster.local:4954
// - $TRIVY_DEBUG=true  if you want better trivy logging

def call (Map args = [
                        project: '',  // GCP project id to run commands against (except for CloudBuild which is always run in --project "$GCR_PROJECT" environment variable to share the same docker images from a shared build project
                        region: '',   // GCP compute region
                        app: '',      // App name - used by ArgoCD
                        version: '',  // tags docker images with this, or $GIT_COMMIT if version is not defined
                        env: '',      // Environment, eg, 'uk-dev', 'us-staging' etc.. - suffixed to ArgoCD app name calls and used if k8s_dir not defined
                        env_vars: [:],  // a Map of environment variables and their values to load to the pipeline
                        creds: [:],     // a Map of environment variable keys and credentials IDs to populate each one with
                        gcp_serviceaccount_key: '',  // the Jenkins secret credential id of the GCP service account auth key
                        gcr_registry: '',  // eg. 'eu.gcr.io' or 'us.gcr.io'
                        images: [],   // List of docker image names (not prefixed by GCR/GAR registries) to test for existence to skip CloudBuild if all are present
                        k8s_dir: '',  // the Kubernetes GitOps repo's directory to Kustomize the image tags in before triggering ArgoCD
                        cloudflare_email: '',  // if both cloudflare email and zone id are set causes a Cloudflare Cache Purge at the end of the pipeline
                        cloudflare_zone_id: '',
                        timeoutMinutes: 60,  // total pipeline timeout limit to catch stuck pipelines
                      ]
      ) {

  pipeline {

    agent {
      kubernetes {
        defaultContainer 'gcloud-sdk'
        yamlFile "ci/jenkins-pod.yaml"
      }
    }

    // backup to catch GitHub -> Jenkins webhook failures
    triggers {
      pollSCM('H/10 * * * *')
    }

    options {
      buildDiscarder(logRotator(numToKeepStr: '30'))
      timestamps()
      timeout (time: "${args.timeoutMinutes ?: 60}", unit: 'MINUTES')
    }

    environment {
      APP         = "${args.app}"
      ENVIRONMENT = "${args.env}"

      CLOUDSDK_CORE_PROJECT   = "${args.project}"
      CLOUDSDK_COMPUTE_REGION = "${args.region}"

      GCP_SERVICEACCOUNT_KEY = credentials("${args.gcp_serviceaccount_key}")

      GCR_REGISTRY = "${args.gcr_registry}"

      ARGOCD_AUTH_TOKEN  = credentials('argocd-auth-token')
      CLOUDFLARE_API_KEY = credentials('cloudflare-api-key')
      GITHUB_TOKEN       = credentials('github-token')

      CLOUDFLARE_EMAIL   = "${args.cloudflare_email ?: ''}"
      CLOUDFLARE_ZONE_ID = "${args.cloudflare_zone_id ?: ''}"
    }

    stages {

      stage('Setup') {
        steps {
          script {
            env.VERSION = "${args.version ?: ''}" ?: "$GIT_COMMIT"        // CloudBuild tags docker images with this $VERSION variable
            env.K8S_DIR = "${args.k8s_dir ?: ''}" ?: "$APP/$ENVIRONMENT"  // Directory path in the GitOps Kubernetes repo in which to Kustomize edit the docker image tag versions

            if (env_vars) {
              if (env_vars instanceof Map == false) {
                error "env_vars passed to parametered pipeline 'gcpBuildDeployKubernetesPipeline' must be a Map"
              }
              env_vars.each { k, v ->
                env[k] = v
              }
            }
            if (creds) {
              if (creds instanceof Map == false) {
                error "creds passed to parametered pipeline 'gcpBuildDeployKubernetesPipeline' must be a Map"
              }
              creds.each { k, v ->
                env[k] = credentials(v)
              }
            }
          }
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
            steps {
              catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                downloadTrivy()
              }
            }
          }

          stage('Install NodeJS') {
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
        steps {
          gcpDockerAuth()
        }
      }

      stage('Wait for CloudBuild & GCR') {
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
