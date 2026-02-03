def call(Map config) {
    pipeline {
        agent any

        parameters {
            string(name: 'APP_NAME', defaultValue: config.appName)
            choice(name: 'LANGUAGE', choices: ['python','go','java','node'])
            choice(name: 'ENV', choices: ['dev','qa','prod'])
            booleanParam(name: 'SKIP_TESTS', defaultValue: false)
            booleanParam(name: 'SKIP_SCAN', defaultValue: false)
        }

        environment {
            IMAGE_TAG = "${env.GIT_COMMIT}"
        }

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Lint') {
                steps {
                    script {
                        scanners.LintScanner.run(params.LANGUAGE)
                    }
                }
            }

            stage('Unit Tests') {
                when { expression { !params.SKIP_TESTS } }
                steps {
                    script {
                        scanners.TestRunner.run(params.LANGUAGE)
                    }
                }
            }

            stage('Security Scan') {
                when { expression { !params.SKIP_SCAN } }
                steps {
                    script {
                        scanners.SecurityScanner.run()
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        builders."${params.LANGUAGE.capitalize()}Builder".build()
                    }
                }
            }

            stage('Docker Build & Push') {
                steps {
                    script {
                        docker.DockerBuild.buildAndPush(
                            params.APP_NAME,
                            IMAGE_TAG
                        )
                    }
                }
            }

            stage('Image Scan') {
                steps {
                    script {
                        scanners.ImageScanner.scan(params.APP_NAME, IMAGE_TAG)
                    }
                }
            }

            stage('Trigger Spinnaker') {
                steps {
                    sh """
                    curl -X POST http://spin-gate/api/v1/webhooks/ot \
                    -H 'Content-Type: application/json' \
                    -d '{
                      "app": "${params.APP_NAME}",
                      "image": "${params.APP_NAME}",
                      "tag": "${IMAGE_TAG}",
                      "env": "${params.ENV}"
                    }'
                    """
                }
            }
        }
    }
}
