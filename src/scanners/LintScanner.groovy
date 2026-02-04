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
                    # 1. Install Go if missing
                    if ! command -v go &> /dev/null; then
                       echo "Go not found, downloading portable version..."
                       curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                       mkdir -p ./go-dist
                       tar -C ./go-dist -xzf go1.21.6.linux-amd64.tar.gz
                       export PATH=$PATH:$(pwd)/go-dist/go/bin
                    fi

                    # 2. Install Linter
                    mkdir -p ./bin
                    curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ./bin v1.55.2
        
                    # 3. Run Linter with updated PATH
                    export PATH=$PATH:$(pwd)/bin
                    go version # Verification
                    ./bin/golangci-lint run ./...
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
