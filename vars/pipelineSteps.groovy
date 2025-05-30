def cloneRepository(String repoUrl, String branch = 'main') {
    echo '📥 Cloning repository...'
    deleteDir()
    git url: repoUrl, branch: branch
}

def buildProject() {
    echo '🔧 Building the project (skipping tests)...'
    sh 'mvn clean install'
}

