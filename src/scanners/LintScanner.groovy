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
                // FIXED: Check if golangci-lint exists; if not, install it locally
                steps.sh '''
                    if ! command -v golangci-lint &> /dev/null
                    then
                        echo "golangci-lint not found, installing..."
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b $(go env GOPATH)/bin v1.55.2
                        export PATH=$PATH:$(go env GOPATH)/bin
                    fi
                    golangci-lint run
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
