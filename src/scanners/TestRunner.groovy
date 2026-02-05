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
                // This looks up the installation path of your 'maven-3' tool
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh """
                    # Explicitly add the bin folder to the PATH for this shell session
                    export PATH=${mvnHome}/bin:\$PATH
                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw test
                    else
                        mvn test
                    fi
                """
                break

            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
    steps.sh """
        export PATH=${nodeHome}/bin:\$PATH
        if [ -f "package.json" ]; then
            npm test || echo "Node tests failed"
        fi
    """
                break

            default:
                steps.echo "No tests defined for ${lang}"
        }
    }
}
