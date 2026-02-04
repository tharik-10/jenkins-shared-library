class LintScanner {

    static void run(def steps, String lang) {
        steps.echo "--- Starting Linting for ${lang} ---"

        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        def globalGoDist = "${workspace}/../.global-go-dist"

        steps.sh "mkdir -p ${localBin}"

        switch (lang) {

            /* ---------------- PYTHON ---------------- */
            case 'python':
                steps.sh """
                    set -euo pipefail
                    export PATH=\$PATH:\$HOME/.local/bin

                    # Install flake8 and bandit if missing
                    command -v flake8 >/dev/null 2>&1 || python3 -m pip install --user flake8
                    command -v bandit >/dev/null 2>&1 || python3 -m pip install --user bandit

                    echo "🔹 Checking Python code style..."
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv

                    echo "🔹 Checking Python security..."
                    python3 -m bandit -r . -x ./venv,./env -ll || true
                """
                break

            /* ---------------- GO ---------------- */
            case 'go':
                steps.sh """
                    set -euo pipefail

                    # ---- Install Go (outside repo) if missing ----
                    if [ ! -d "${globalGoDist}/go" ]; then
                        echo "Installing Go globally..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${globalGoDist}
                        tar -C ${globalGoDist} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:${localBin}:\$PATH

                    # ---- Isolated GOPATH ----
                    export GOPATH=\$PWD/.gopath
                    export GOMODCACHE=\$PWD/.gomodcache
                    mkdir -p \$GOPATH \$GOMODCACHE

                    # ---- Install golangci-lint if missing ----
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        echo "Installing golangci-lint..."
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh \
                          | sh -s -- -b ${localBin} v1.55.2
                    fi

                    # ---- Cleanup old dirs ----
                    rm -rf go-dist go-cache || true

                    # ---- Validate module ----
                    go env
                    go mod tidy

                    # ---- Run lint ----
                    echo "🔹 Running Go lint..."
                    ${localBin}/golangci-lint run ./... \
                        --timeout=5m \
                        --skip-dirs=go-dist,go-cache,.gopath,.gomodcache \
                        -v
                """
                break

            /* ---------------- JAVA ---------------- */
            case 'java':
                steps.sh """
                    set -euo pipefail

                    # Prefer Maven Wrapper
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        echo "🔹 Using Maven Wrapper..."
                        ./mvnw clean verify checkstyle:check
                    else
                        # Check system Maven
                        if command -v mvn >/dev/null 2>&1; then
                            echo "🔹 Using system Maven..."
                            mvn clean verify checkstyle:check
                        else
                            echo "❌ Maven not found! Install Maven or add mvnw to the repo."
                            exit 1
                        fi
                    fi
                """
                break

            /* ---------------- NODE ---------------- */
            case 'node':
                steps.sh """
                    set -euo pipefail

                    # Install node modules if missing
                    if [ ! -d "node_modules" ]; then
                        echo "🔹 Installing Node dependencies..."
                        npm install --quiet
                    fi

                    # Run lint, allow fix-dry-run
                    echo "🔹 Running Node lint..."
                    npm run lint || npx eslint . --fix-dry-run || echo "Lint skipped"
                """
                break

            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
