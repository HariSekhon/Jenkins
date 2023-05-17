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
//        G C P   B u i l d   &   D e p l o y   t o   K u b e r n e t e s
// ========================================================================== //

def call (Map args = [
                        project: '',
                        region: '',
                        app: '',
                        version: '',
                        env: '',
                        env_vars: [:],
                        cloudbuild: '',
                        gcp_serviceaccount_key: '',
                        gcr_registry: '',
                        images: [],
                        k8s_dir: '',
                        cloudflare_email: '',
                        cloudflare_zone_id: '',
                        timeoutMinutes: 60,
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
            env.VERSION = "${args.version ?: ''}" ?: "$GIT_COMMIT"
            env.K8S_DIR = "${args.k8s_dir ?: ''}" ?: "$APP/$ENVIRONMENT"

            if (env_vars) {
              if (env_vars instanceof Map == false) {
                error "env_vars passed to parametered pipeline 'gcpBuildDeployKubernetesPipeline' must be a Map"
              }
              env_vars.each { k, v ->
                env[k] = v
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

      stage('GCP CloudBuild'){
        steps {
          gcpCloudBuild(args: args.cloudbuild ?: '--project="$GCR_PROJECT" --substitutions="_REGISTRY=$GCR_REGISTRY,_IMAGE_VERSION=$VERSION,_GIT_BRANCH=${GIT_BRANCH##*/}"',
                        timeoutMinutes: 90,
                        // auto-inferred now
                        //skipIfDockerImagesExist: env.DOCKER_IMAGES.split(',').collect { "$it:$VERSION" }
                        )
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

      stage('Container Image Scanning') {
        parallel {
          // being extremely slow, and hit this bug giving no useful results anyway, so disable for now:
          //
          //    https://github.com/quay/clair/issues/1756
          //
          //stage('Clair') {
          //  steps {
          //    catchError (buildResult: 'SUCCESS', stageResult: 'FAILURE') {
          //      clair()
          //    }
          //  }
          //}
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
