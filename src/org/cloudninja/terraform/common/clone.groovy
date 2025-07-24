package org.cloudninja.terraform.common

class gitclone {
    def clone(String branch, String repoUrl, String credentialsId = null) {
        stage('Cloning Repository') {
            echo "Cloning ${repoUrl} branch: ${branch}"
            if (credentialsId) {
                git branch: branch, url: repoUrl, credentialsId: credentialsId
            } else {
                git branch: branch, url: repoUrl
            }
        }
    }
}
