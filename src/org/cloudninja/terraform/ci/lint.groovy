package org.cloudninja.terraform.ci

def tflintScan(String terraformDir) {
    stage('Lint') {
        dir(terraformDir) {
            sh """
                if ! command -v ./tflint-bin/tflint &> /dev/null; then
                    echo "Installing TFLint locally..."
                    mkdir -p tflint-bin
                    curl -sL https://github.com/terraform-linters/tflint/releases/latest/download/tflint_linux_amd64.zip -o tflint.zip
                    unzip -o tflint.zip -d tflint-bin
                    chmod +x tflint-bin/tflint
                fi

                ./tflint-bin/tflint --format json > tflint_report.json || true
            """
            archiveArtifacts artifacts: 'tflint_report.json', fingerprint: true
        }
    }
}
