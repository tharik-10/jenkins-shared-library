package org.cloudninja.terraform.ci

def terraformPlan(String terraformDir, String tfvarsFile = '') {
    stage('Terraform Plan') {
        dir(terraformDir) {
            sh 'terraform init'

            if (tfvarsFile?.trim()) {
                // Normalize the tfvars path
                def tfvarsPath = tfvarsFile.trim()
                // Check if the file exists before running plan
                def tfvarsExists = sh(script: "test -f ${tfvarsPath}", returnStatus: true) == 0

                if (!tfvarsExists) {
                    error "Terraform tfvars file '${tfvarsPath}' not found. Please check the path."
                }

                // Run plan with valid tfvars file
                sh "terraform plan -out=tfplan.binary -var-file=${tfvarsPath}"
            } else {
                error "Terraform plan requires a tfvars file. Please provide it (e.g., ../testing/basic-usage/terraform.tfvars)."
            }

            sh 'terraform show -json tfplan.binary > plan_output.json'
            archiveArtifacts artifacts: 'plan_output.json', fingerprint: true
        }
    }
}
