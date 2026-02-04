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
                steps.sh '''
                    npm audit fix || true
                    npm audit || true
                '''
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
                steps.sh """
                    if [ -f "mvnw" ]; then
                        ./mvnw org.owasp:dependency-check-maven:check || echo "Dependency check failed"
                    else
                        mvn org.owasp:dependency-check-maven:check || echo "Maven not found, skipping OWASP scan"
                    fi
                """
                break
                
            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}
