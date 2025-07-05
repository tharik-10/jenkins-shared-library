def call(Map config = [:]) {
  def steps = this

  def MODULE_DIR = config.get('tfModuleDir', '')
  def TF_VARS    = config.get('tfVars', [:])
  def ACTION     = config.get('action', 'apply')  // accepts "apply" or "destroy"

  def tf = new org.cloudninja.TerraformUtils(steps)

  node {
    try {
      stage("Terraform ${ACTION.capitalize()}") {
        if (ACTION == 'apply') {
          tf.terraformApply(directory: MODULE_DIR, vars: TF_VARS)
        } else if (ACTION == 'destroy') {
          tf.terraformDestroy(directory: MODULE_DIR, vars: TF_VARS)
        } else {
          error "Unsupported action: ${ACTION}. Use 'apply' or 'destroy'."
        }
      }

      currentBuild.result = 'SUCCESS'
    } catch (err) {
      currentBuild.result = 'FAILURE'
      throw err
    }
  }
}
