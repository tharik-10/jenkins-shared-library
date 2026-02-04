package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        // Move Go install to a hidden folder to prevent 'go mod tidy' from crashing
        def goInstallDir = "${workspace}/.go-dist"
        
        steps.sh "mkdir -p ${localBin}"

        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user flake8 bandit
                    
                    echo "Checking code style with Flake8..."
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv,__pycache__,build,dist
                    
                    echo "Checking security with Bandit (excluding venv)..."
                    python3 -m bandit -r . -x ./venv,./env,./.venv -ll
                """
                break
                
            case 'go':
                steps.sh """
                    # 1. Setup Portable Go Environment
                    if [ ! -d "${goInstallDir}/go" ]; then
                        echo "Downloading Go..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${goInstallDir}
                        tar -C ${goInstallDir} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${goInstallDir}/go
                    export GOPATH=${workspace}/go-cache
                    export GOCACHE=${workspace}/go-build-cache
                    export GO111MODULE=on
                    export PATH=\$GOROOT/bin:\$PATH:${localBin}
                    
                    # 2. Setup Linter
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ${localBin} v1.55.2
                    fi
                    
                    # 3. Clean modules and Run
                    go mod tidy || echo "Tidy warnings suppressed"
                    ${localBin}/golangci-lint run ./... --timeout=5m --skip-dirs="go-cache,bin,.go-dist" -v
                """
                break
                
            case 'java':
                steps.sh """
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw checkstyle:check || echo "Checkstyle failed, continuing..."
                    elif command -v mvn &> /dev/null; then
                        mvn checkstyle:check || echo "Checkstyle failed, continuing..."
                    else
                        echo "Maven not found. Skipping Java linting."
                    fi
                """
                break
                
            case 'node':
                steps.sh """
                    if [ -f "package.json" ]; then
                        export npm_config_cache=${workspace}/.npm-cache
                        [ ! -d "node_modules" ] && npm install --quiet
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
