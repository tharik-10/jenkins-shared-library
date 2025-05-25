def call(String gitUser, String gitEmail, String commitMessage, String credId, String repoUrl, String branch = 'main') {
    sh """
        git config user.name '${gitUser}'
        git config user.email '${gitEmail}'
        echo "Pipeline ran on: \$(date)" > pipeline-log.txt
        git add pipeline-log.txt
        git commit -m '${commitMessage}' -s || echo 'No changes to commit.'
    """

    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh '''
           git remote set-url origin https://${USERNAME}:${PASSWORD}@${repoUrl}
           git checkout ${branch}
           git pull origin ${branch}
           git push origin ${branch} || echo "Nothing to push."
        '''
    }
    echo "✅ Commit sign-off and push completed."
}
