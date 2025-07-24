import org.cloudninja.terraform.common.*
import org.cloudninja.terraform.ci.*

def call(Map args) {
    String terraformDir  = args.terraformDir
    String branch        = args.branch
    String repoUrl       = args.repoUrl
    String credentialsId = args.credentialsId ?: null
    String tfvarsFile    = args.tfvarsFile ?: ''  

    node {
        // Instantiate helper classes
        def Clean    = new wsclean(this)
        def Clone    = new gitclone(this)
        def Init     = new init(this)
        def Fmt      = new fmt(this)
        def Validate = new validate(this)
        def Lint     = new lint(this)
        def Checkov  = new checkov(this)
        def Plan     = new plan(this)

        // Run CI steps
        Clean.clean()
        Clone.clone(branch, repoUrl, credentialsId)
        Init.terraformInit(terraformDir)
        Fmt.terraformFormat(terraformDir)
        Validate.terraformValidate(terraformDir)
        Lint.tflintScan(terraformDir)
        Checkov.terraformcheckov(terraformDir)
        Plan.terraformPlan(terraformDir, tfvarsFile)  
    }
}
