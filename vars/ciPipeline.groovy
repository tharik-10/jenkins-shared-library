import scanners.*
import builders.*
import docker.*

def call(Map config = [:]) {
    def serviceDirMap = [
        attendance: 'attendance',
        employee  : 'employee',
        salary    : 'salary',
        frontend  : 'frontend'
    ]

    def serviceLangMap = [
        attendance: 'python',
        employee  : 'go',
        salary    : 'java',
        frontend  : 'node'
    ]

    pipeline {
        agent any

        parameters {
            choice(name: 'SERVICE', choices: serviceDirMap.keySet() as List, description: 'Select microservice')
            choice(name: 'ENV', choices: ['dev','qa','prod'], description: 'Target environment')
            // ADDED THE WEBHOOK PARAMETER HERE
            string(name: 'SPINNAKER_WEBHOOK', defaultValue: 'http://abb741232a1614d6da3765409de0c5b0-634014634.us-east-1.elb.amazonaws.com/api/v1/webhooks/ot', description: 'Spinnaker Webhook URL')
            booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip unit tests')
            booleanParam(name: 'SKIP_SCAN', defaultValue: false, description: 'Skip security scans')
        }

        environment {
            SERVICE_DIR = "${serviceDirMap[params.SERVICE]}"
            LANGUAGE    = "${serviceLangMap[params.SERVICE]}"
            APP_NAME    = "${params.SERVICE}-api"
            IMAGE_TAG   = "${env.GIT_COMMIT}"
            ECR_URL     = "574621078554.dkr.ecr.us-east-1.amazonaws.com"
        }

        stages {
            stage('Checkout') {
                steps { checkout scm }
            }

            stage('CI Pipeline') {
                steps {
                    script {
                        dir(env.SERVICE_DIR) {
                            scanners.LintScanner.run(this, env.LANGUAGE)

                            if (!params.SKIP_TESTS) {
                                scanners.TestRunner.run(this, env.LANGUAGE)
                            }

                            if (!params.SKIP_SCAN) {
                                scanners.SecurityScanner.run(this, env.LANGUAGE) 
                            }

                            switch(env.LANGUAGE) {
                                case 'python': new builders.PythonBuilder().build(this); break
                                case 'go':     new builders.GoBuilder().build(this); break
                                case 'java':   new builders.JavaBuilder().build(this); break
                                case 'node':   new builders.NodeBuilder().build(this); break
                                default: error "No builder for ${env.LANGUAGE}"
                            }

                            docker.DockerBuild.buildAndPush(this, env.ECR_URL, env.APP_NAME, env.IMAGE_TAG)
                            scanners.ImageScanner.scan(this, env.ECR_URL, env.APP_NAME, env.IMAGE_TAG)
                        }
                    }
                }
            }
            
            stage('Trigger Spinnaker') {
                steps {
                    // UPDATED TO USE params.SPINNAKER_WEBHOOK
                    sh """
                    curl -X POST ${params.SPINNAKER_WEBHOOK} \
                    -H 'Content-Type: application/json' \
                    -d '{"app": "${APP_NAME}", "image": "${APP_NAME}", "tag": "${IMAGE_TAG}", "env": "${params.ENV}"}'
                    """
                }
            }
        }

        post {
            success { echo "✅ CI completed successfully for ${APP_NAME}" }
            failure { echo "❌ CI failed for ${APP_NAME}" }
        }
    }
}
