package org.cloudninja.terraform.ci

def terraformPlan(String terraformDir, String tfvarsFile = '') {
    stage('Terraform Plan') {
        dir(terraformDir) {
            sh 'terraform init'

            if (tfvarsFile?.trim()) {
                def tfvarsPath = tfvarsFile.trim()
                
                // Check existence relative to current dir
                def tfvarsExists = sh(script: "test -f '${tfvarsPath}'", returnStatus: true) == 0
                if (!tfvarsExists) {
                    error "Terraform tfvars file '${tfvarsPath}' not found in ${terraformDir}. Please check the path."
                }

                // Run plan
                sh "terraform plan -out=tfplan.binary -var-file=${tfvarsPath}"
            } else {
                error "Terraform plan requires a tfvars file. Provide it relative to '${terraformDir}', e.g., 'terraform.tfvars'"
            }

            sh 'terraform show -json tfplan.binary > plan_output.json'
            archiveArtifacts artifacts: 'plan_output.json', fingerprint: true
        }
    }
}
