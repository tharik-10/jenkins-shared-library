package org.cloudninja.terraform.cd

def call(String terraform_path, String tfvarsFile = "", String exactStatePath = "") {
    stage('Terraform Destroy') {
        echo "Destroying Terraform configuration in ${exactStatePath}..."

        sh """
            echo "Switching to directory: ${exactStatePath}"
            cd "${exactStatePath}"

            terraform init -input=false
            terraform destroy -auto-approve -var-file=${tfvarsFile}
        """
    }
}
