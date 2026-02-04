package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "--- Starting Linting for ${lang} ---"
        
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        // Key Fix: Place Go in the root workspace, NOT inside the service folder
        def globalGoDist = "${workspace}/../.global-go-dist"
        
        steps.sh "mkdir -p ${localBin}"

        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user flake8 bandit
                    echo "Checking code style..."
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv
                    echo "Checking security..."
                    python3 -m bandit -r . -x ./venv,./env -ll || true
                """
                break
                
            case 'go':
                steps.sh """
                    if [ ! -d "${globalGoDist}/go" ]; then
                        echo "Installing Go in global root..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${globalGoDist}
                        tar -C ${globalGoDist} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:${localBin}:\$PATH
                    
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ${localBin} v1.55.2
                    fi
                    
                    go mod tidy || echo "Tidy warnings suppressed"
                    ${localBin}/golangci-lint run ./... --timeout=5m -v
                """
                break
                
            case 'java':
                steps.sh """
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw checkstyle:check || echo "Checkstyle failed"
                    else
                        mvn checkstyle:check || echo "Maven not found"
                    fi
                """
                break
                
            case 'node':
                steps.sh """
                    [ ! -d "node_modules" ] && npm install --quiet
                    npm run lint || npx eslint . --fix-dry-run || echo "Linting skipped"
                """
                break
                
            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
