package org.cloudninja.terraform.common

def clone(String branch, String repoUrl, String credentialsId = null) {
    stage('cloning repositories') {
        echo "Cloning repository from ${repoUrl}, branch: ${branch}"

        if (credentialsId) {
            echo "Using credentials ID: ${credentialsId}"
            git branch: branch, url: repoUrl, credentialsId: credentialsId
        } else {
            git branch: branch, url: repoUrl
        }
    }
}
