package org.cloudninja.terraform.cd

def terraformInit(String terraformDir) {
    stage('init') {
        dir(terraformDir) {
            sh 'terraform init'
        }
    }
}
