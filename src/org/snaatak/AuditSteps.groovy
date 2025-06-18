package org.opstree

class AuditSteps implements Serializable {

    def script
    AuditSteps(script) {
        this.script = script
    }

    def installPackages() {
        script.stage('Install Packages') {
            script.sh '''
                sudo apt-get update
                sudo apt-get install -y python3 python3-pip python3-venv jq
            '''
        }
    }

    def cloneRepositories(String attendanceRepo, String notificationRepo) {
        script.stage('Clone Repositories') {
            script.sh """
                rm -rf attendance-api notification-worker
                git clone ${attendanceRepo}
                git clone ${notificationRepo}
            """
        }
    }

    def auditAttendance() {
        script.stage('Audit Attendance API') {
            script.dir('attendance-api') {
                script.sh '''
                    python3 -m venv venv1
                    . venv1/bin/activate
                    pip install pip-audit
                    pip freeze > requirements.txt
                    pip-audit -r requirements.txt --format json > pip-audit-report.json

                    mkdir -p ../html-reports/attendance
                    echo "<h2>Attendance API - pip-audit Report</h2><pre>$(cat pip-audit-report.json | jq .)</pre>" > ../html-reports/attendance/index.html
                    deactivate
                '''
                script.archiveArtifacts artifacts: 'pip-audit-report.json', allowEmptyArchive: true
            }
        }
    }

    def auditNotification() {
        script.stage('Audit Notification Worker') {
            script.dir('notification-worker') {
                script.sh '''
                    python3 -m venv venv2
                    . venv2/bin/activate
                    pip install pip-audit
                    pip freeze > requirements.txt
                    pip-audit -r requirements.txt --format json > pip-audit-report.json

                    mkdir -p ../html-reports/notification
                    echo "<h2>Notification Worker - pip-audit Report</h2><pre>$(cat pip-audit-report.json | jq .)</pre>" > ../html-reports/notification/index.html
                    deactivate
                '''
                script.archiveArtifacts artifacts: 'pip-audit-report.json', allowEmptyArchive: true
            }
        }
    }

    def publishReports() {
        script.stage('Publish pip-audit Reports') {
            script.script {
                if (script.fileExists('html-reports/attendance/index.html') &&
                    script.fileExists('html-reports/notification/index.html')) {
                    script.publishHTML([
                        reportDir: 'html-reports/attendance',
                        reportFiles: 'index.html',
                        reportName: 'pip-audit Report - Attendance API',
                        keepAll: true,
                        alwaysLinkToLastBuild: true
                    ])
                    script.publishHTML([
                        reportDir: 'html-reports/notification',
                        reportFiles: 'index.html',
                        reportName: 'pip-audit Report - Notification Worker',
                        keepAll: true,
                        alwaysLinkToLastBuild: true
                    ])
                } else {
                    script.echo 'HTML reports not found. Skipping publishing.'
                }
            }
        }
    }
}
