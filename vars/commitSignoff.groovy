def prepareWorkspace() {
    echo '🧽 Cleaning workspace before checkout...'
    deleteDir()
}

def checkoutCode(String repoUrl, String branch, String credentialsId) {
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
        sh """
            git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@${repoUrl} .
            git checkout ${branch}
            git pull origin ${branch} --rebase
        """
    }
}

def configureGit(String userName, String userEmail) {
    sh """
        git config user.name "${userName}"
        git config user.email "${userEmail}"
    """
}

def printCommitMessage() {
    def message = sh(script: "git log -1 --pretty=format:'%B'", returnStdout: true).trim()
    echo "📝 Latest Commit Message:\n${message}"
}
