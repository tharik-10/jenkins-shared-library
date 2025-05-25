def call(Map config = [:]) {
    def gitUser = config.gitUser ?: "default-user"
    def gitEmail = config.gitEmail ?: "default@example.com"
    def commitMessage = config.commitMessage ?: "Default commit message"

    echo "🔧 Configuring Git user and email..."
    sh """
    git config user.name "${gitUser}"
    git config user.email "${gitEmail}"
    """

    echo "📄 Making dummy change to pipeline-log.txt..."
    sh """
    echo "Pipeline ran on: \$(date)" > pipeline-log.txt
    git add pipeline-log.txt
    """

    echo "✅ Committing with sign-off..."
    sh """
    git commit -m "${commitMessage}" -s || echo "No changes to commit."
    """

    echo "🚀 Pushing changes to remote..."
    withCredentials([usernamePassword(credentialsId: 'github-token1', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh """
        git push https://\${USERNAME}:\${PASSWORD}@github.com/tharik-10/sprint-3.git HEAD:main || echo "Nothing to push."
        """
    }

    echo "📝 Printing latest commit message..."
    def message = sh(script: "git log -1 --pretty=format:'%B'", returnStdout: true).trim()
    echo "📝 Latest Commit Message:\n${message}"
}
