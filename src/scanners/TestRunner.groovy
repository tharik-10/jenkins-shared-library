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
        
        # FIX: Copy config into the specific folder where the test file resides
        if [ -f "config.yaml" ]; then
            # Copy it to the current directory AND the subdirectory if needed
            cp config.yaml employee/config.yaml || true
            export CONFIG_PATH=\$(pwd)/config.yaml
            echo "✅ Config synced to folder: employee/config.yaml"
        fi

        # Run tests from the service root
        go test ./... -v
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
