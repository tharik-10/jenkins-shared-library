package scanners

class LintScanner implements Serializable {

    static void run(def steps, String lang) {
        steps.echo "--- Starting Linting for ${lang} ---"

        def workspace     = steps.pwd()
        def localBin      = "${workspace}/bin"
        def globalGoDist  = "${workspace}/../.global-go-dist"

        steps.sh "mkdir -p ${localBin}"

        switch (lang) {

            case 'python':
                steps.sh '''
                    export PATH="$PATH:$HOME/.local/bin"
                    python3 -m pip install --user flake8 bandit || true
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv
                    python3 -m bandit -r . -x ./venv -ll || true
                '''
                break

            case 'go':
                steps.sh """
                    set -e

                    # Install Go if not present
                    if [ ! -d "${globalGoDist}/go" ]; then
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${globalGoDist}
                        tar -C ${globalGoDist} -xzf go1.21.6.linux-amd64.tar.gz
                    fi
                """

                steps.sh '''
                    export GOROOT="${PWD}/../.global-go-dist/go"
                    export PATH="$GOROOT/bin:'"${localBin}"':$PATH"

                    # Install golangci-lint if missing
                    if [ ! -f "'"${localBin}"'/golangci-lint" ]; then
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh \
                          | sh -s -- -b "'"${localBin}"'" v1.55.2
                    fi

                    pwd
                    ls -la

                    go mod tidy

                    echo "🔍 Running golangci-lint..."
                    golangci-lint run ./... --timeout=5m --skip-dirs=vendor
                '''
                break

            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh "export PATH=${mvnHome}/bin:\$PATH && mvn checkstyle:checkstyle || echo 'Lint warnings found'"
                break

            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'

steps.sh """
    set -e

    export PATH=${nodeHome}/bin:\$PATH

    echo "--- Starting Linting for node ---"
    node --version
    npm --version

    if [ -f "eslint.config.js" ]; then
        echo "✅ ESLint config found"

        # Ensure local ESLint exists (required for flat config)
        if [ ! -x "node_modules/.bin/eslint" ]; then
            echo "📦 Installing local ESLint..."
            npm install --no-audit --no-fund --save-dev eslint@^8
        fi

        echo "🔎 ESLint version:"
        npx eslint --version

        echo "🧹 Running ESLint..."
        npx eslint src || echo "⚠️ ESLint completed with warnings/errors"
    else
        echo "⚠️ ESLint config not found, skipping lint"
    fi
"""

                break
        }
    }
}
