import org.cloudninja.terraform.common.*
import org.cloudninja.terraform.cd.*

def call(Map args) {
    String terraformDir     = args.terraformDir
    String branch           = args.branch
    String repoUrl          = args.repoUrl
    String credentialsId    = args.credentialsId ?: null
    String tfvarsFile       = args.tfvarsFile ?: ''
    boolean enableDestroy   = args.get('enableDestroy', false)
    String approvalMessage  = args.get('approvalMessage', 'Approve Terraform Apply?')
    String exactStatePath   = args.get('exactStatePath', '') // Newly added line

    node {
        // Instantiate helper classes
        def Clean    = new wsclean()
        def Clone    = new gitclone()
        def Init     = new init()
        def Plan     = new plan()
        def Apply    = new terraform_apply()
        def Destroy  = new terraform_destroy()
        def Approve  = new manualapproval()

        // ❌ Skip cleanWs if destroy mode is enabled
        if (!enableDestroy) {
            Clean.clean()
        }

        Clone.clone(branch, repoUrl, credentialsId)
        Init.terraformInit(terraformDir)

        // Only plan if not destroying
        if (!enableDestroy) {
            Plan.terraformPlan(terraformDir, tfvarsFile)
        }

        // Manual approval before apply/destroy
        Approve(approvalMessage)

        if (enableDestroy) {
            Destroy.call(exactStatePath, tfvarsFile, exactStatePath) // ✅ Pass exactStatePath here
        } else {
            Apply(exactStatePath, tfvarsFile)
        }
    }
}
