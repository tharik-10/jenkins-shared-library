package org.cloudninja.terraform.ci

def terraformInit(String terraformDir) {
    stage('init') {
        dir(terraformDir) {
            sh 'terraform init'
        }
    }
}
