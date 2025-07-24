package org.cloudninja.terraform.common

def call(String message) {
    stage('Manual  Approval') {
    input message: message, ok: 'Proceed'
    }
}
