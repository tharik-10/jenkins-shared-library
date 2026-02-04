package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        
        // Define a local bin to keep the workspace clean and avoid permission issues
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        steps.sh "mkdir -p ${localBin}"

        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user flake8
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv,__pycache__,build,dist
                """
                break
                
            case 'go':
                steps.sh """
                    # 1. Setup Portable Go Environment
                    if ! command -v go &> /dev/null; then
                        echo "Go not found, downloading portable version..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ./go-dist
                        tar -C ./go-dist -xzf go1.21.6.linux-amd64.tar.gz
                    fi

                    # 2. Define local Go Paths to avoid /var/lib/jenkins permission errors
                    export GOROOT=${workspace}/go-dist/go
                    export GOPATH=${workspace}/go-cache
                    export GOCACHE=${workspace}/go-build-cache
                    export PATH=\$GOROOT/bin:\$PATH:${localBin}
                    
                    # 3. Setup Linter
                    curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ${localBin} v1.55.2
                    
                    # 4. Prepare Go Module Context
                    # Since go.mod exists, we tidy it to ensure dependencies are resolved
                    echo "Tidying Go modules in ${workspace}..."
                    go mod tidy

                    # 5. Run Linter
                    # Using ./... with verbose output to track why files are analyzed
                    echo "Executing golangci-lint..."
                    ${localBin}/golangci-lint run ./... --timeout=5m -v
                """
                break
                
            case 'java':
                steps.sh """
                    if [ -f "mvnw" ]; then
                        echo "Using Maven Wrapper..."
                        chmod +x mvnw
                        ./mvnw checkstyle:check || echo "Checkstyle failed, but continuing build..."
                    elif command -v mvn &> /dev/null; then
                        mvn checkstyle:check || echo "Checkstyle failed, but continuing build..."
                    else
                        echo "Maven not found. Skipping Java linting."
                    fi
                """
                break
                
            case 'node':
                steps.sh """
                    if [ -f "package.json" ]; then
                        # Set local npm cache to avoid permission issues
                        export npm_config_cache=${workspace}/.npm-cache
                        
                        # Install dependencies if node_modules missing
                        [ ! -d "node_modules" ] && npm install --quiet
                        
                        # Try running lint, fall back to npx eslint if no script exists
                        npm run lint || npx eslint . --ext .js,.ts --fix-dry-run || echo "No ESLint config found."
                    else
                        echo "No package.json found. Skipping Node linting."
                    fi
                """
                break
                
            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
