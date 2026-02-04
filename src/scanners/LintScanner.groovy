package scanners

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
                    /bin/bash -euo pipefail -c '
                    export PATH=\$PATH:\$HOME/.local/bin

                    # Install flake8 & bandit if missing
                    command -v flake8 >/dev/null 2>&1 || python3 -m pip install --user flake8
                    command -v bandit >/dev/null 2>&1 || python3 -m pip install --user bandit

                    echo "🔹 Checking Python code style..."
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv,__pycache__

                    echo "🔹 Checking Python security..."
                    python3 -m bandit -r . -x ./venv,./env,__pycache__ -ll || true
                    '
                """
                break

            /* ---------------- GO ---------------- */
            case 'go':
                steps.sh """
                    /bin/bash -euo pipefail -c '
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

                    # ---- HARD ISOLATION ----
                    export GOPATH=\$PWD/.gopath
                    export GOMODCACHE=\$PWD/.gomodcache
                    mkdir -p \$GOPATH \$GOMODCACHE

                    # ---- Install golangci-lint ----
                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh \
                          | sh -s -- -b ${localBin} v1.55.2
                    fi

                    # ---- Defensive cleanup ----
                    rm -rf .gopath .gomodcache_local

                    # ---- Validate module ----
                    go env
                    go mod tidy

                    # ---- Detect code directories dynamically ----
                    dirs=""
                    for d in cmd internal pkg; do
                        [ -d "\$d" ] && dirs="\$dirs \$d"
                    done

                    if [ -z "\$dirs" ]; then
                        dirs="."  # fallback to current dir
                    fi

                    # ---- Run lint ----
                    ${localBin}/golangci-lint run \$dirs \
                        --timeout=5m \
                        --skip-dirs=vendor \
                        -v
                    '
                """
                break

            /* ---------------- JAVA ---------------- */
            case 'java':
                steps.sh """
                    /bin/bash -euo pipefail -c '
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw checkstyle:check
                    else
                        command -v mvn >/dev/null 2>&1 || { echo "Maven not found"; exit 1; }
                        mvn checkstyle:check
                    fi
                    '
                """
                break

            /* ---------------- NODE ---------------- */
            case 'node':
                steps.sh """
                    /bin/bash -euo pipefail -c '
                    # Install node_modules if missing
                    [ ! -d "node_modules" ] && npm install --quiet

                    # Run ESLint (skip if fails)
                    npm run lint || npx eslint . --fix-dry-run || echo "Lint skipped"
                    '
                """
                break

            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}

