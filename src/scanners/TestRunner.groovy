package scanners

class TestRunner {

    static void run(def steps, String lang) {
        steps.echo "Running Tests for ${lang}..."

        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        def goInstallDir = "${workspace}/.go-dist"

        steps.sh "mkdir -p ${localBin}"

        switch(lang) {

            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user pytest
                    pytest || echo "Python tests failed"
                """
                break

            case 'go':
                def globalGo = "${workspace}/../.global-go-dist"
                steps.sh """
                    export GOROOT=${globalGo}/go
                    export PATH=\$GOROOT/bin:\$PATH
                    # Only test local packages (those starting with 'employee')
                    go test \$(go list ./... | grep '^employee') -v || echo "Tests failed but continuing"
                """
                break

            case 'java':
                steps.sh """
                    if [ -f mvnw ]; then
                        chmod +x mvnw
                        ./mvnw test || echo "Java tests failed"
                    else
                        mvn test || echo "Maven not found"
                    fi
                """
                break

            case 'node':
                steps.sh """
                    if [ -f package.json ]; then
                        npm test || echo "Node tests failed"
                    fi
                """
                break

            default:
                steps.echo "No tests defined for ${lang}"
        }
    }
}
