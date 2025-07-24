package org.cloudninja.terraform.common

class wsclean implements Serializable {
    def steps

    wsclean(steps) {
        this.steps = steps
    }

    def clean() {
        steps.sh 'rm -rf * .terraform*'
    }
}
