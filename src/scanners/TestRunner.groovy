package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Unit Tests for ${lang}..."
        switch(lang) {
            case 'python':
                steps.sh '''
                # Install all required dependencies for the app and tests
                python3 -m pip install --user pytest flask mysql-connector-python PyYAML elastic-apm
                
                # Run pytest and accept exit code 5 (no tests found)
                python3 -m pytest . --import-mode=importlib || [ $? -eq 5 ]
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
