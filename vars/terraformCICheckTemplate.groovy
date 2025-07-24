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
        def Clean    = new wsclean()
        def Clone    = new gitclone()
        def Init     = new init()
        def Fmt      = new fmt()
        def Validate = new validate()
        def Lint     = new lint()
        def Checkov  = new checkov()
        def Plan     = new plan()

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
