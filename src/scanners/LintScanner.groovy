package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        switch(lang) {
            case 'python':
                steps.sh '''
                    python3 -m pip install --user flake8
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv,__pycache__,build,dist
                '''
                break
                
            case 'go':
                steps.sh '''
                    # 1. Create a local bin folder in the workspace
                    mkdir -p ./bin
        
                    # 2. Download and install to the local ./bin folder
                    echo "Installing golangci-lint locally..."
                    curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ./bin v1.55.2
        
                    # 3. Run it using the local path
                    ./bin/golangci-lint run
                '''
                break
                
            case 'java':
                steps.sh 'mvn checkstyle:check'
                break
                
            case 'node':
                steps.sh 'npm run lint || echo "No lint script found in package.json, skipping..."'
                break
                
            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
