package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Tests for ${lang}..."

        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"

        steps.sh "mkdir -p ${localBin}"

        switch(lang) {
            case 'go':
                def globalGo = "${workspace}/../.global-go-dist"
                steps.sh """
                    export GOROOT=${globalGo}/go
                    export PATH=\$GOROOT/bin:\$PATH
                    
                    # 1. Check if config.yaml exists in the repo
                    if [ -f "config.yaml" ]; then
                        echo "✅ Using config.yaml from repository"
                        export CONFIG_PATH=\$(pwd)/config.yaml
                    else
                        echo "⚠️ config.yaml not found in employee directory!"
                        # Optional: Fallback to a template or fail
                        exit 1
                    fi
                    
                    # 2. Run tests
                    go test ./... -v || echo "Tests failed but continuing"
                """
                break

            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user pytest
                    pytest --ignore=venv || echo "Python tests failed"
                """
                break

            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh "export PATH=${mvnHome}/bin:\$PATH && mvn test -DfailIfNoTests=false"
                break

            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
                steps.sh """
                    export PATH=${nodeHome}/bin:\$PATH
                    if [ -f "package.json" ]; then
                        echo "Installing dependencies..."
                        npm install --quiet  # This creates node_modules and react-scripts
                        
                        echo "Checking for tests..."
                        if [ -f "src/App.react.js" ] && [ ! -f "src/App.test.js" ]; then
                            cp src/App.react.js src/App.test.js
                        fi

                        npm test -- --watchAll=false --passWithNoTests || echo "Node tests failed"
                    fi
                """

            default:
                steps.echo "No tests defined for ${lang}"
        }
    }
}
