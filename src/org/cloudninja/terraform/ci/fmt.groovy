package org.cloudninja.terraform.ci

def terraformFormat(String terraformDir) {
    stage('fmt') {
    dir(terraformDir) {
        sh 'terraform fmt '
    }
    }
}
