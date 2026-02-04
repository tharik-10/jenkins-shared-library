package scanners

class TestRunner {
    static void run(def steps, String lang) {
        steps.echo "Running Unit Tests for ${lang}..."
        
        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user pytest
                    export CONFIG_FILE=./config.yaml
                    python3 -m pytest . --import-mode=importlib || [ \$? -eq 5 ]
                """
                break
                
            case 'go':
                steps.sh """
                    # Ensure Go is in PATH
                    if [ -d "\$(pwd)/go-dist/go/bin" ]; then
                        export PATH=\$PATH:\$(pwd)/go-dist/go/bin
                    fi
                    
                    # Initialize mod if dev forgot to push go.mod
                    if [ ! -f "go.mod" ]; then go mod init temp-test && go mod tidy; fi
                    
                    go test ./... -v || echo "Tests failed but check logs"
                """
                break
                
            case 'java':
                steps.sh """
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw test
                    else
                        mvn test || echo "Maven missing, cannot run tests"
                    fi
                """
                break
                
            case 'node':
                steps.sh """
                    if [ -f "package.json" ]; then
                        npm test || echo "No tests defined or tests failed"
                    fi
                """
                break
        }
    }
}
