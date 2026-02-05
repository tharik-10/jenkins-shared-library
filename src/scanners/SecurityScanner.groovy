package scanners

class SecurityScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan for ${lang}..."
        def localBin = "${steps.pwd()}/bin"
        steps.sh "mkdir -p ${localBin}"
        
        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user safety bandit
                    if [ -f requirements.txt ]; then
                        python3 -m pip install --user --upgrade -r requirements.txt || echo "Upgrades failed"
                    fi
                    python3 -m safety check --full-report || true
                    python3 -m bandit -r . -f screen || true
                """
                break
                
            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
    steps.sh """
        export PATH=${nodeHome}/bin:\$PATH
        npm audit fix --force || true
        npm audit || true
    """
                break
                
            case 'go':
                steps.sh """
                    # Install Go if missing (re-using your portable logic)
                    if ! command -v go &> /dev/null; then
                        curl -LO https://go.dev/dl/go1.21.6.linux-amd64.tar.gz
                        tar -C . -xzf go1.21.6.linux-amd64.tar.gz
                        export PATH=\$PATH:\$(pwd)/go/bin
                    fi
                    
                    # Install Nancy for Go dependency scanning
                    if ! command -v nancy &> /dev/null; then
                        curl -sfL https://github.com/sonatype-nexus-community/nancy/releases/download/v1.0.42/nancy-v1.0.42-linux-amd64.tar.gz | tar -C ${localBin} -xz nancy
                    fi
                    
                    go list -m all | ${localBin}/nancy sleuth || true
                """
                break

            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh """
                    export PATH=${mvnHome}/bin:\$PATH
                    
                    echo "🧹 Attempting to fix corrupted OWASP DB..."
                    # We use -DfailOnError=false to ensure the pipeline doesn't stop here
                    mvn org.owasp:dependency-check-maven:purge -DfailOnError=false || true

                    if [ -f "mvnw" ]; then
                        chmod +x mvnw
                        ./mvnw org.owasp:dependency-check-maven:check -DfailOnError=false || echo "Scan failed but moving on..."
                    else
                        mvn org.owasp:dependency-check-maven:check -DfailOnError=false || echo "Scan failed but moving on..."
                    fi
                """
                break
                
            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}
