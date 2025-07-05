package org.cloudninja

class TerraformCDUtils implements Serializable {
  def steps

  TerraformCDUtils(steps) {
    this.steps = steps
  }

  def terraformApply(Map config) {
    def dir = config.directory
    def varsArgs = config.vars?.collect { k, v -> "-var '${k}=${v}'" }?.join(' ') ?: ''
    steps.sh "cd ${dir} && terraform apply -auto-approve ${varsArgs}"
  }

  def terraformDestroy(Map config) {
    def dir = config.directory
    def varsArgs = config.vars?.collect { k, v -> "-var '${k}=${v}'" }?.join(' ') ?: ''
    steps.sh "cd ${dir} && terraform destroy -auto-approve ${varsArgs}"
  }
}
