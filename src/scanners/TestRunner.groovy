package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Unit Tests for ${lang}..."
        switch(lang) {
            case 'python':
                steps.sh '''
                python3 -m pip install --user pytest
                # Use . to search the entire service directory
                python3 -m pytest . --import-mode=importlib
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
