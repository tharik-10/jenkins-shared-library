package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Unit Tests for ${lang}..."
        switch(lang) {
            case 'python':
                // Install pytest first, then run it
                steps.sh '''
                python3 -m pip install --user pytest
                python3 -m pytest
                '''
                break
            case 'go':
                steps.sh 'go test ./...'
                break
            case 'java':
                steps.sh 'mvn test'
                break
            case 'node':
                steps.sh 'npm test'
                break
        }
    }
}
