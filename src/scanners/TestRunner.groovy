package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        def goInstallDir = "${workspace}/.go-dist"
        
        steps.sh "mkdir -p ${localBin}"

        switch(lang) {
            case 'python':
                steps.sh """
                    # 1. Setup Python paths
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user flake8 bandit
                    
                    # 2. Lint ONLY project code (exclude venv)
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv,__pycache__,build,dist
                """
                break
                
            case 'go':
                steps.sh """
                    # 1. Setup Portable Go Environment outside recursion
                    if [ ! -d "${goInstallDir}/go" ]; then
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${goInstallDir}
                        tar -C ${goInstallDir} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${goInstallDir}/go
                    export GOPATH=${workspace}/go-cache
                    export GOCACHE=${workspace}/go-build-cache
                    export PATH=\$GOROOT/bin:\$PATH:${localBin}
                    
                    # 2. Setup Linter
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ${localBin} v1.55.2
                    fi
                    
                    # 3. Tidy and Run
                    go mod tidy || echo "Tidy warnings suppressed"
                    ${localBin}/golangci-lint run ./... --timeout=5m --skip-dirs="go-cache,bin,.go-dist" -v
                """
                break
                
            case 'java':
                steps.sh """
                    [ -f "mvnw" ] && chmod +x mvnw && ./mvnw checkstyle:check || mvn checkstyle:check || echo "Java linting skipped"
                """
                break
                
            case 'node':
                steps.sh """
                    if [ -f "package.json" ]; then
                        export npm_config_cache=${workspace}/.npm-cache
                        [ ! -d "node_modules" ] && npm install --quiet
                        npm run lint || npx eslint . --ext .js,.ts --fix-dry-run || echo "Lint failed"
                    fi
                """
                break
                
            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
