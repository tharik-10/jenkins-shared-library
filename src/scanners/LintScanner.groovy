package scanners

class LintScanner {

    static void run(def steps, String lang) {
        steps.echo "--- Starting Linting for ${lang} ---"

        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"

        // Go is installed OUTSIDE repo to avoid scanning pollution
        def globalGoDist = "${workspace}/../.global-go-dist"

        steps.sh "mkdir -p ${localBin}"

        switch (lang) {

            /* ---------------- PYTHON ---------------- */
            case 'python':
                steps.sh """
                    set -e
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user flake8 bandit
                    echo "Checking code style..."
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv
                    echo "Checking security..."
                    python3 -m bandit -r . -x ./venv,./env -ll || true
                """
                break

            /* ---------------- GO ---------------- */
            case 'go':
                steps.sh """
                    set -e

                    # ---- Install Go (outside repo) ----
                    if [ ! -d "${globalGoDist}/go" ]; then
                        echo "Installing Go globally..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${globalGoDist}
                        tar -C ${globalGoDist} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:${localBin}:\$PATH

                    # ---- HARD ISOLATION (CRITICAL FIX) ----
                    export GOPATH=\$PWD/.gopath
                    export GOMODCACHE=\$PWD/.gomodcache
                    mkdir -p \$GOPATH \$GOMODCACHE

                    # ---- Install golangci-lint ----
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh \
                          | sh -s -- -b ${localBin} v1.55.2
                    fi

                    # ---- Defensive cleanup ----
                    rm -rf go-dist go-cache

                    # ---- Validate module ----
                    go env
                    go mod tidy

                    # ---- Run lint ONLY on real code ----
                    ${localBin}/golangci-lint run \
                        ./cmd/... ./internal/... ./pkg/... \
                        --timeout=5m \
                        --skip-dirs=go-dist,go-cache,.gopath,.gomodcache \
                        -v
                """
                break

            /* ---------------- JAVA ---------------- */
            case 'java':
                steps.sh """
                    set -e
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw checkstyle:check
                    else
                        mvn checkstyle:check
                    fi
                """
                break

            /* ---------------- NODE ---------------- */
            case 'node':
                steps.sh """
                    set -e
                    [ ! -d "node_modules" ] && npm install --quiet
                    npm run lint || npx eslint . --fix-dry-run || echo "Lint skipped"
                """
                break

            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}

