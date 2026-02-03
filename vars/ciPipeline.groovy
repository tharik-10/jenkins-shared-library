def call(Map config) {

    def serviceDirMap = [
        attendance: 'attendance-api',
        employee  : 'employee-api',
        salary    : 'salary-api',
        frontend  : 'frontend'
    ]

    pipeline {
        agent any

        parameters {
            choice(
                name: 'SERVICE',
                choices: serviceDirMap.keySet() as List
            )
            choice(
                name: 'LANGUAGE',
                choices: ['python','go','java','node']
            )
            choice(
                name: 'ENV',
                choices: ['dev','qa','prod']
            )
            booleanParam(name: 'SKIP_TESTS', defaultValue: false)
            booleanParam(name: 'SKIP_SCAN', defaultValue: false)
        }

        environment {
            APP_NAME   = "${params.SERVICE}-api"
            IMAGE_TAG = "${env.GIT_COMMIT}"
            SERVICE_DIR = "${serviceDirMap[params.SERVICE]}"
            ECR_URL    = credentials('ecr-url')   // OR plain string if public
            IMAGE_TAG = "${env.GIT_COMMIT}"
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

                            scanners.LintScanner.run(params.LANGUAGE)

                            if (!params.SKIP_TESTS) {
                                scanners.TestRunner.run(params.LANGUAGE)
                            }

                            if (!params.SKIP_SCAN) {
                                scanners.SecurityScanner.run()
                            }

                            builders."${params.LANGUAGE.capitalize()}Builder".build()

                            docker.DockerBuild.buildAndPush(
                                env.ECR_URL,
                                env.APP_NAME,
                                env.IMAGE_TAG
                            )

                            scanners.ImageScanner.scan(
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
    }
}
