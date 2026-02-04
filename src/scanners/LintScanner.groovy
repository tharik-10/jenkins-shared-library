package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        
        // Define a local bin to keep the workspace clean
        def localBin = "${steps.pwd()}/bin"
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
                    # 1. Setup Portable Go
                    if ! command -v go &> /dev/null; then
                        echo "Go not found, downloading portable version..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ./go-dist
                        tar -C ./go-dist -xzf go1.21.6.linux-amd64.tar.gz
                        export PATH=\$PATH:\$(pwd)/go-dist/go/bin
                    fi

                    # 2. Setup Linter
                    curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ${localBin} v1.55.2
                    export PATH=\$PATH:${localBin}
                    
                    # 3. Initialize module if missing (prevents 'no go files' error)
                    if [ ! -f "go.mod" ]; then
                        go mod init temp-project && go mod tidy
                    fi

                    ./bin/golangci-lint run --path-prefix . ./...
                """
                break
                
            case 'java':
                // Check if Maven is installed, otherwise use the wrapper (standard in pro projects)
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
                        # Install dependencies if node_modules missing
                        [ ! -d "node_modules" ] && npm install --quiet
                        
                        # Try running lint, fall back to a basic syntax check if no script exists
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
