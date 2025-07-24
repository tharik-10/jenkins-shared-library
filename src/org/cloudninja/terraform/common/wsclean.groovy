package org.cloudninja.terraform.ci

class wsclean implements Serializable {
    def steps

    wsclean(steps) {
        this.steps = steps
    }

    def clean() {
        steps.sh 'rm -rf * .terraform*'
    }
}
