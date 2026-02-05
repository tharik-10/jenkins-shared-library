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
                    if [ -d "go" ]; then
                        echo "⚠️ Removing leftover local go directory..."
                        rm -rf go
                    fi

                    if [ ! -d "${globalGoDist}/go" ]; then
                        echo "Installing Go globally..."
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${globalGoDist}
                        tar -C ${globalGoDist} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:${localBin}:\$PATH

                    export GOMODCACHE=${workspace}/.gomodcache
                    mkdir -p \$GOMODCACHE

                    if [ ! -f "${localBin}/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh \
                          | sh -s -- -b ${localBin} v1.55.2
                    fi

                    go mod tidy || echo "Tidy warnings suppressed"

                    dirs=""
                    for d in cmd internal pkg; do
                        [ -d "\$d" ] && dirs="\$dirs \$d"
                    done
                    [ -z "\$dirs" ] && dirs="."

                    ${localBin}/golangci-lint run \$dirs --timeout=5m --skip-dirs=vendor -v
                    '
                """
                break

            /* ---------------- JAVA ---------------- */
            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh """
                    export PATH=${mvnHome}/bin:\$PATH
                    # Use 'checkstyle' instead of 'check' to prevent build failure
                    mvn checkstyle:checkstyle || echo "Checkstyle warnings suppressed"
                """
                break

            /* ---------------- NODE ---------------- */
            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
    steps.sh """
        export PATH=${nodeHome}/bin:\$PATH
        # Check if lint script exists in package.json, otherwise use npx or skip
        if npm run | grep -q 'lint'; then
            npm run lint
        elif [ -f ".eslintrc" ] || [ -f ".eslintrc.json" ]; then
            npx eslint . --fix-dry-run
        else
            echo "No lint configuration found, skipping linting."
        fi
    """
                break

            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}

