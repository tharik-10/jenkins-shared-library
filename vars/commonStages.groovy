def call(Map config = [:]) {
    stage('Verify Code') {
        echo 'Listing files from copied repo...'
        sh 'ls -la'
    }

    stage('Build') {
        echo 'Building the salary-api (skipping tests)...'
        sh 'mvn clean install -DskipTests=true'
    }
}
