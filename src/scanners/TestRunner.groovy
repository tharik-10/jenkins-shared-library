package scanners

class TestRunner {
    static void run(String lang) {
        switch(lang) {
            case 'python':
                sh 'pytest'
                break
            case 'go':
                sh 'go test ./...'
                break
            case 'java':
                sh 'mvn test'
                break
            case 'node':
                sh 'npm test -- --watch=false'
                break
        }
    }
}

