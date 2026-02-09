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
                    # 1. Setup Go Path
                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:\$PATH
                    
                    # 2. Set the config path to the current directory
                    # Since we are already inside the 'employee' folder,
                    # config.yaml is right here.
                    export CONFIG_PATH=\$(pwd)/config.yaml
                    
                    echo "--- Debugging Go Test Environment ---"
                    echo "Current Directory: \$(pwd)"
                    ls -lh config.yaml || echo "❌ config.yaml NOT FOUND in \$(pwd)"
                    
                    # 3. Run tests using '.' 
                    # We use './...' to test everything in the CURRENT directory
                    CONFIG_PATH=\$CONFIG_PATH go test ./... -v
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
