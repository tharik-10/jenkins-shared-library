package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Unit Tests for ${lang}..."
        switch(lang) {
            case 'python':
                steps.sh '''
                # Check if requirements.txt exists and install dependencies
                if [ -f requirements.txt ]; then
                    python3 -m pip install --user -r requirements.txt
                fi
                
                # Install pytest explicitly just in case it's not in requirements.txt
                python3 -m pip install --user pytest
                
                # Run pytest
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
