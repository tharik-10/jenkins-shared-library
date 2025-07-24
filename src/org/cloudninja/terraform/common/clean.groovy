package org.cloudninja.terraform.common

class wsclean {
    def clean() {
        stage('Clean Workspace') {
            cleanWs() // Correct built-in Jenkins command
        }
    }
}
