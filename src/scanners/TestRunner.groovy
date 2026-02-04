package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Unit Tests for ${lang}..."
        switch(lang) {
            case 'python':
                steps.sh '''
                # Install/Upgrade dependencies
                if [ -f requirements.txt ]; then
                    python3 -m pip install --user --upgrade -r requirements.txt
                fi
                
                python3 -m pip install --user pytest
                
                # Point to the config.yaml located in the service directory
                export CONFIG_FILE=./config.yaml
                
                # Run pytest (Exit code 5 means no tests found, which we allow)
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
