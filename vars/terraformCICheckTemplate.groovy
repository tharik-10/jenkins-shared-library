def call(Map config = [:]) {
  def steps = this

  def MODULE_DIR    = config.get('tfModuleDir', '')
  def BACKEND_CONF  = config.get('backendConfig', [:])
  def TF_VARS       = config.get('tfVars', [:])
  def PLAN_OUT_FILE = config.get('planOutFile', 'tfplan.out')

  def tf = new org.snaatak.TerraformCIUtils(steps)

  node {
    try {
      stage('Checkout Code') {
        checkout scm
      }

      stage('Terraform Init') {
        tf.terraformInit(
          directory: MODULE_DIR,
          backendConfig: BACKEND_CONF
        )
      }

      stage('Terraform Validate') {
        tf.terraformValidate(
          directory: MODULE_DIR
        )
      }

      stage('Terraform Plan') {
        tf.terraformPlan(
          directory: MODULE_DIR,
          vars: TF_VARS,
          outFile: PLAN_OUT_FILE
        )
      }

      currentBuild.result = 'SUCCESS'

    } catch (err) {
      currentBuild.result = 'FAILURE'
      throw err
    }
  }
}
