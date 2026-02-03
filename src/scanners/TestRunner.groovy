package scanners

class TestRunner {

    static void run(def script, String language) {

        switch(language) {
            case 'python':
                script.sh 'pytest'
                break

            case 'go':
                script.sh 'go test ./...'
                break

            case 'java':
                script.sh 'mvn test'
                break

            case 'node':
                script.sh 'npm test'
                break
        }
    }
}
