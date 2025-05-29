// vars/cloneAndPost.groovy
def call(Map config = [:]) {
    def attendanceRepo = config.attendanceRepo ?: 'https://github.com/OT-MICROSERVICES/attendance-api.git'
    def notificationRepo = config.notificationRepo ?: 'https://github.com/OT-MICROSERVICES/notification-worker.git'

    echo "📦 Cloning repositories..."
    sh "git clone ${attendanceRepo}"
    sh "git clone ${notificationRepo}"
}

def handleSuccess() {
    echo '🎉 Build succeeded!'
}

def handleFailure() {
    echo '❌ Build failed!'
}

def handleUnstable() {
    echo '⚠️ Audit found vulnerabilities.'
}

def handleAlways() {
    echo '🧹 Cleaning up workspace...'
    deleteDir()
}


