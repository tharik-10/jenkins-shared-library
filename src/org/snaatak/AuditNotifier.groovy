package org.snaatak

class AuditNotifier implements Serializable {
    def sendNotification(script, status, priority, slackChannel, emailRecipients, attendanceUrl, notificationUrl) {
        def icons = [SUCCESS: '🟢', FAILURE: '🔴', UNSTABLE: '🟡']
        def colors = [SUCCESS: 'good', FAILURE: 'danger', UNSTABLE: 'warning']
        def buildTime = new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('Asia/Kolkata'))
        def triggeredBy = script.currentBuild.getBuildCauses().find { it.userId }?.userName ?: "Automated/Unknown"

        def message = """
${icons[status]} *${priority} ${status}*
*Job:* ${script.env.JOB_NAME}
*Build:* #${script.env.BUILD_NUMBER}
*By:* ${triggeredBy}
*Time:* ${buildTime}
🔗 <${script.env.BUILD_URL}|View Build>
📄 Attendance Report: <${attendanceUrl}|Report>
📄 Notification Report: <${notificationUrl}|Report>
"""

        script.slackSend(channel: slackChannel, color: colors[status], message: message)
        script.mail(to: emailRecipients, subject: "${priority} ${status}: ${script.env.JOB_NAME}", body: message.replaceAll(/\*/,''))
    }
}
