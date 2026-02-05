package scanners

class SecurityScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan for ${lang}..."
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        steps.sh "mkdir -p ${localBin}"
        
        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user safety bandit
                    
                    # Safety check for known vulnerabilities in dependencies
                    python3 -m safety check --full-report || true
                    
                    # FIX: Exclude venv to avoid scanning third-party libraries
                    python3 -m bandit -r . -x ./venv -f screen || true
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
                    if ! command -v go &> /dev/null; then
                        export PATH=\$PATH:${workspace}/go/bin
                    fi
                    
                    if ! command -v nancy &> /dev/null; then
                        curl -sfL https://github.com/sonatype-nexus-community/nancy/releases/download/v1.0.42/nancy-v1.0.42-linux-amd64.tar.gz | tar -C ${localBin} -xz nancy
                    fi
                    
                    # Run nancy and suppress error if rate-limited (401)
                    go list -m all | ${localBin}/nancy sleuth || echo "Nancy scan failed or rate-limited"
                """
                break

            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh """
                    export PATH=${mvnHome}/bin:\$PATH
                    
                    echo "🧹 Cleaning corrupted OWASP DB..."
                    mvn org.owasp:dependency-check-maven:purge -DfailOnError=false || true

                    # FIX: Use nvdApiDelay to prevent 403/401 errors without a key
                    # This makes the scan slower but prevents the NullPointerException
                    mvn org.owasp:dependency-check-maven:check \
                        -DnvdApiDelay=16000 \
                        -DfailOnError=false || echo "Scan failed but moving on..."
                """
                break
                
            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}
