package org.cloudninja

class TerraformCIUtils implements Serializable {
  def steps

  TerraformCIUtils(steps) {
    this.steps = steps
  }

  def terraformInit(Map config) {
    def dir = config.directory
    def backendConfig = config.backendConfig?.collect { k, v -> "-backend-config=${k}=${v}" }?.join(' ') ?: ''
    steps.sh "cd ${dir} && terraform init ${backendConfig}"
  }

  def terraformValidate(Map config) {
    def dir = config.directory
    steps.sh "cd ${dir} && terraform validate"
  }

  def terraformPlan(Map config) {
    def dir = config.directory
    def varsArgs = config.vars?.collect { k, v -> "-var '${k}=${v}'" }?.join(' ') ?: ''
    def outFileArg = config.outFile ? "-out=${config.outFile}" : ''
    steps.sh "cd ${dir} && terraform plan ${varsArgs} ${outFileArg}"
  }

  def runCheckov(Map config) {
    def dir = config.directory
    steps.sh "cd ${dir} && checkov -d . -o json > checkov-report.json || true"
    steps.archiveArtifacts artifacts: "${dir}/checkov-report.json", fingerprint: true
  }

  def runTFLint(Map config) {
    def dir = config.directory
    steps.sh "cd ${dir} && tflint --init && tflint > tflint.log || true"
    steps.archiveArtifacts artifacts: "${dir}/tflint.log", fingerprint: true
  }
}
