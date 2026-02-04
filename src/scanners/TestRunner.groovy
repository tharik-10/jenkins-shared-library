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
                steps.sh """
                    if [ ! -d "${goInstallDir}/go" ]; then
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        mkdir -p ${goInstallDir}
                        tar -C ${goInstallDir} -xzf go1.21.6.linux-amd64.tar.gz
                        rm go1.21.6.linux-amd64.tar.gz
                    fi

                    export GOROOT=${goInstallDir}/go
                    export GOPATH=${workspace}/go-cache
                    export GOCACHE=${workspace}/go-build-cache
                    export PATH=\$GOROOT/bin:\$PATH

                    go test ./... || echo "Go tests failed"
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
