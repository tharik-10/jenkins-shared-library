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
            // Even though we build one image, we might still want to trigger the pipeline 
            // based on a specific service change, or just select 'all'
            choice(name: 'ENV', choices: ['dev','qa','prod'], description: 'Target environment')
            string(name: 'SPINNAKER_WEBHOOK', defaultValue: 'http://aa40b02b7d7f94df48fec6c66beb9080-1633709375.us-east-1.elb.amazonaws.com:8084/webhooks/webhook/ot-microservices', description: 'Spinnaker Webhook URL')
            booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip unit tests')
            booleanParam(name: 'SKIP_SCAN', defaultValue: false, description: 'Skip security scans')
        }

        environment {
            // Name of the single monolith repository in ECR
            MONO_REPO_NAME = "ot-microservices"
            IMAGE_TAG      = "${env.GIT_COMMIT}"
            ECR_URL        = "574621078554.dkr.ecr.us-east-1.amazonaws.com"
        }

        stages {
            stage('Checkout') {
                steps { checkout scm }
            }

            stage('Quality Gates (All Services)') {
                steps {
                    script {
                        // Validate every service before building the monolith
                        serviceDirMap.each { svcName, svcDir ->
                            def lang = serviceLangMap[svcName]
                            echo "--- Validating Service: ${svcName} (${lang}) ---"
                            dir(svcDir) {
                                scanners.LintScanner.run(this, lang)
                                if (!params.SKIP_TESTS) {
                                    scanners.TestRunner.run(this, lang)
                                }
                                if (!params.SKIP_SCAN) {
                                    scanners.SecurityScanner.run(this, lang)
                                }
                            }
                        }
                    }
                }
            }

            stage('Monolith Build & Push') {
                steps {
                    script {
                        // This now runs at the ROOT where the Makefile is
                        docker.DockerBuild.buildAndPush(this, env.ECR_URL, env.MONO_REPO_NAME, env.IMAGE_TAG)
                        
                        // Scan the resulting monolith image
                        scanners.ImageScanner.scan(this, env.ECR_URL, env.MONO_REPO_NAME, env.IMAGE_TAG)
                    }
                }
            }

            stage('Trigger Spinnaker') {
                steps {
                    sh """
                    curl -X POST ${params.SPINNAKER_WEBHOOK} \
                    -H 'Content-Type: application/json' \
                    -d '{
                        "app": "ot-microservices", 
                        "image": "${env.MONO_REPO_NAME}", 
                        "tag": "${env.IMAGE_TAG}", 
                        "env": "${params.ENV}"
                    }'
                    """
                }
            }
        }

        post {
            success { echo "✅ Monolith CI completed: ${env.MONO_REPO_NAME}:${env.IMAGE_TAG}" }
            failure { echo "❌ CI failed for Monolith build" }
        }
    }
}
