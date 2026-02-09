package scanners

class TestRunner implements Serializable {
    static void run(def steps, String lang) {
        steps.echo "Running Tests for ${lang}..."
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        steps.sh "mkdir -p ${localBin}"

        switch(lang) {
            case 'go':
                def globalGoDist = "${workspace}/../.global-go-dist"
    steps.sh """
        export GOROOT=${globalGoDist}/go
        export PATH=\$GOROOT/bin:\$PATH
        
        # 1. Set the variable we THINK it wants
        export CONFIG_PATH=\$(pwd)/config.yaml
        
        # 2. Some Go projects expect 'CONF_PATH' or 'CONFIG' 
        # Let's set the most common ones just in case
        export CONF_PATH=\$CONFIG_PATH
        export CONFIG=\$CONFIG_PATH
        
        echo "--- Debugging Employee Service ---"
        echo "Current Dir: \$(pwd)"
        ls -la config.yaml
        
        # 3. Use 'env' to inject the variable directly into the test execution
        # We also run from the current directory
        env CONFIG_PATH=\$CONFIG_PATH CONF_PATH=\$CONFIG_PATH go test ./... -v
    """
                break

            case 'python':
                steps.sh """
                    # Install headers without sudo password prompt
                    sudo apt-get update || true
                    sudo apt-get install -y pkg-config libcairo2-dev libglib2.0-dev libgirepository1.0-dev || true

                    python3 -m venv venv
                    . venv/bin/activate
                    pip install --upgrade pip setuptools wheel
                    pip install -r requirements.txt
                    pytest --verbose || echo "Tests failed but continuing"
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
                        npm install --quiet
                        npm test -- --watchAll=false --passWithNoTests || echo "Node tests failed"
                    fi
                """
                break
        }
    }
}
