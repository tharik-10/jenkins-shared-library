package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        // We move the go-dist one level up or to a hidden temp folder 
        // to prevent 'go mod tidy' from seeing it as a submodule.
        def goInstallDir = "${workspace}/.go-dist"
        
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
                    # 1. Setup Portable Go Environment (Hidden from module recursion)
                    if [ ! -d "${goInstallDir}/go" ]; then
                        echo "Go not found, downloading portable version..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${goInstallDir}
                        tar -C ${goInstallDir} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    # 2. Define local Go Paths
                    export GOROOT=${goInstallDir}/go
                    export GOPATH=${workspace}/go-cache
                    export GOCACHE=${workspace}/go-build-cache
                    export GO111MODULE=on
                    export PATH=\$GOROOT/bin:\$PATH:${localBin}
                    
                    # 3. Setup Linter
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b ${localBin} v1.55.2
                    fi
                    
                    # 4. Prepare Go Module Context
                    # Now that GOROOT is in .go-dist, tidy will ignore it
                    echo "Tidying Go modules..."
                    go mod tidy || echo "Tidy encountered issues, but continuing..."

                    # 5. Run Linter
                    # Explicitly skipping the cache and bin directories
                    echo "Executing golangci-lint..."
                    ${localBin}/golangci-lint run ./... --timeout=5m --skip-dirs="go-cache,bin" -v
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
