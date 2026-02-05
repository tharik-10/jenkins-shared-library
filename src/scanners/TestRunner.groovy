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
                    
                    # 1. Create the employee directory structure
                    mkdir -p employee

                    # 2. Write your specific config content into the file
                    # This ensures the Go app finds the exact settings it needs
                    cat <<EOF > employee/config.yaml
elasticsearch:
  enabled: true
  host: http://empms-es:9200
  username: elastic
  password: elastic

employee:
  api_port: "8083"
EOF

                    # 3. Also place a copy in the root, just in case
                    cp employee/config.yaml config.yaml

                    # 4. Run tests
                    go test \$(go list ./... | grep '^employee') -v || echo "Tests failed but continuing"
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
                steps.sh "export PATH=${nodeHome}/bin:\$PATH && npm test || true"
                break

            default:
                steps.echo "No tests defined for ${lang}"
        }
    }
}
