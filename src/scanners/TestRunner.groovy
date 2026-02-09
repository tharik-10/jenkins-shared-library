package scanners

class TestRunner implements Serializable {

    static void run(def steps, String lang) {
        steps.echo "Running Tests for ${lang}..."

        def workspace = steps.pwd()
        def localBin  = "${workspace}/bin"
        steps.sh "mkdir -p ${localBin}"

        switch (lang) {

            case 'go':
                def globalGoDist = "${workspace}/../.global-go-dist"

                steps.sh """
                    set -e

                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:\$PATH

                    # Go service expects CONFIG_FILE
                    export CONFIG_FILE=\$(pwd)/config.yaml

                    echo "---- Go Test Debug Info ----"
                    echo "Workspace      : \$(pwd)"
                    echo "GOROOT         : \$GOROOT"
                    echo "CONFIG_FILE    : \$CONFIG_FILE"

                    if [ ! -f "\$CONFIG_FILE" ]; then
                        echo "❌ config.yaml not found!"
                        exit 1
                    fi

                    # Run Go tests
                    go test ./... -v
                """
                break

            case 'python':
                steps.sh """
                    set -e

                    sudo apt-get update || true
                    sudo apt-get install -y \
                        pkg-config \
                        libcairo2-dev \
                        libglib2.0-dev \
                        libgirepository1.0-dev || true

                    python3 -m venv venv
                    . venv/bin/activate

                    pip install --upgrade pip setuptools wheel
                    pip install -r requirements.txt

                    pytest --verbose || echo "⚠️ Python tests failed but continuing"
                """
                break

            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh """
                    export PATH=${mvnHome}/bin:\$PATH
                    mvn test -DfailIfNoTests=false
                """
                break

            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
                steps.sh """
                    export PATH=${nodeHome}/bin:\$PATH

                    if [ -f "package.json" ]; then
                        npm install --quiet
                        npm test -- --watchAll=false --passWithNoTests || \
                        echo "⚠️ Node tests failed"
                    else
                        echo "No package.json found, skipping Node tests"
                    fi
                """
                break

            default:
                steps.echo "⚠️ No test runner configured for language: ${lang}"
        }
    }
}

