def call(String username, String email, String message, String credentialsId, String repoUrl, String branch = 'main') {
    sh """
        git config user.name '${username}'
        git config user.email '${email}'
        echo "Pipeline ran on: \$(date)" > pipeline-log.txt
        git add pipeline-log.txt
        git commit -m '${message}' -s || echo 'No changes to commit.'
    """

    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh '''
            git remote set-url origin https://${USERNAME}:${PASSWORD}@${repoUrl}
            git push origin HEAD:${branch} || echo "Nothing to push."
        '''
    }
    echo "✅ Commit sign-off and push completed."
}
