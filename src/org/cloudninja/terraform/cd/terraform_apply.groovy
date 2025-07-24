package org.cloudninja.terraform.cd

def call(String terraform_path, String tfvarsFile = "") {
    stage('Terraform apply') {
        echo "Applying Terraform configuration in ${terraform_path}..."

        dir(terraform_path) {
            // Copy the correct binary plan file
            // sh 'cp /var/lib/jenkins/workspace/fill/the/correct/path//tfplan.binary .'

            // Apply the binary plan file
            sh 'terraform apply -input=false -auto-approve tfplan.binary'
        }
    }
}
