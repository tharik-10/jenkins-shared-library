import scanners.*
import builders.*
import docker.*
    
def call(Map config = [:]) {

    /* ==========================
       Service → Directory Map
       ========================== */
    def serviceDirMap = [
        attendance: 'attendance',
        employee  : 'employee',
        salary    : 'salary',
        frontend  : 'frontend'
    ]

    /* ==========================
       Service → Language Map
       (Avoids wrong selection)
       ========================== */
    def serviceLangMap = [
        attendance: 'python',
        employee  : 'go',
        salary    : 'java',
        frontend  : 'node'
    ]

    pipeline {
        agent any

        parameters {
            choice(
                name: 'SERVICE',
                choices: serviceDirMap.keySet() as List,
                description: 'Select microservice to build'
            )
            choice(
                name: 'ENV',
                choices: ['dev','qa','prod'],
                description: 'Target environment'
            )
            booleanParam(
                name: 'SKIP_TESTS',
                defaultValue: false,
                description: 'Skip unit tests'
            )
            booleanParam(
                name: 'SKIP_SCAN',
                defaultValue: false,
                description: 'Skip security scans'
            )
        }

        environment {
            SERVICE_DIR = "${serviceDirMap[params.SERVICE]}"
            LANGUAGE    = "${serviceLangMap[params.SERVICE]}"
            APP_NAME    = "${params.SERVICE}-api"
            IMAGE_TAG   = "${env.GIT_COMMIT}"
            ECR_URL     = "123456789012.dkr.ecr.ap-south-1.amazonaws.com"
        }

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('CI Pipeline') {
                steps {
                    script {
                        dir(env.SERVICE_DIR) {

                            /* ---------- LINT ---------- */
                            scanners.LintScanner.run(this, env.LANGUAGE)

                            /* ---------- TESTS ---------- */
                            if (!params.SKIP_TESTS) {
                                scanners.TestRunner.run(this, env.LANGUAGE)
                            }

                            /* ---------- SECURITY SCAN ---------- */
                            if (!params.SKIP_SCAN) {
                               // Add 'env.LANGUAGE' here
                               scanners.SecurityScanner.run(this, env.LANGUAGE) 
                            }

                            /* ---------- BUILD ---------- */
                            builders."${env.LANGUAGE.capitalize()}Builder".build(this)

                            /* ---------- DOCKER BUILD & PUSH ---------- */
                            docker.DockerBuild.buildAndPush(
                                this,
                                env.ECR_URL,
                                env.APP_NAME,
                                env.IMAGE_TAG
                            )

                            /* ---------- IMAGE SCAN ---------- */
                            scanners.ImageScanner.scan(
                                this,
                                env.ECR_URL,
                                env.APP_NAME,
                                env.IMAGE_TAG
                            )
                        }
                    }
                }
            }

            stage('Trigger Spinnaker') {
                steps {
                    sh """
                    curl -X POST http://spin-gate/api/v1/webhooks/ot \
                    -H 'Content-Type: application/json' \
                    -d '{
                      "app": "${APP_NAME}",
                      "image": "${APP_NAME}",
                      "tag": "${IMAGE_TAG}",
                      "env": "${ENV}"
                    }'
                    """
                }
            }
        }

        post {
            success {
                echo "✅ CI completed successfully for ${APP_NAME}"
            }
            failure {
                echo "❌ CI failed for ${APP_NAME}"
            }
        }
    }
}
