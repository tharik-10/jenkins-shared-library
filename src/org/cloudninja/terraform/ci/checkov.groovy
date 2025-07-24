package org.cloudninja.terraform.ci

def terraformcheckov(String terraformDir) {
    stage('Checkov') {
        dir(terraformDir) {
            sh '''
                if ! command -v checkov &> /dev/null; then
                    pip install --user -q --upgrade checkov
                fi
                export PATH=$HOME/.local/bin:$PATH

                checkov -d . --output cli | tee checkov_report.txt

                PASSED_COUNT=$(grep -oP 'Passed checks: \\K\\d+' checkov_report.txt || echo 0)
                echo "Total Passed Checks: $PASSED_COUNT"
            '''
            archiveArtifacts artifacts: 'checkov_report.txt', fingerprint: true
        }
    }
}
