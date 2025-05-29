// vars/cloneAndPost.groovy
def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            ATTENDANCE_REPO   = config.attendanceRepo ?: 'https://github.com/OT-MICROSERVICES/attendance-api.git'
            NOTIFICATION_REPO = config.notificationRepo ?: 'https://github.com/OT-MICROSERVICES/notification-worker.git'
        }

        stages {
            stage('Clone Repositories') {
                steps {
                    echo 'Cloning repositories...'
                    sh '''
                        git clone ${ATTENDANCE_REPO}
                        git clone ${NOTIFICATION_REPO}
                    '''
                }
            }
        }

        post {
            success {
                echo '🎉 Build succeeded!'
            }
            failure {
                echo '❌ Build failed!'
            }
            unstable {
                echo '⚠️ Audit found vulnerabilities.'
            }
            always {
                echo 'Cleaning workspace...'
                deleteDir()
            }
        }
    }
}
