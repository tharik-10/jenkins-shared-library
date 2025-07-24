package org.cloudninja.terraform.ci

def terraformValidate(String terraformDir) {
    stage('validate') {
    dir(terraformDir) {
        sh 'terraform validate'
    }
}
} 
