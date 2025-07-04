def call(Map config) {
  def tf = new org.terraformci.TerraformUtils(this)
  tf.terraformValidate(config)
}
