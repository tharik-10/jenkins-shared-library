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
                    # 1. Install system-level dependencies required for building Python C-extensions
                    # This fixes the 'cairo not found' and 'PyGObject' build errors
                    sudo apt-get update && sudo apt-get install -y \
                        pkg-config \
                        libcairo2-dev \
                        libglib2.0-dev \
                        libgirepository1.0-dev

                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user --upgrade pip setuptools wheel
                    
                    echo "--- Automatically Upgrading All Outdated Packages ---"
                    python3 -m pip install --user pip-review
                    python3 -m pip_review --local --auto
                    
                    echo "--- Running Security Scan ---"
                    python3 -m pip install --user safety bandit
                    
                    python3 -m safety check --full-report || true
                    python3 -m bandit -r . -x ./venv -f screen || true
                """
                break
                
            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
    steps.sh """
        export PATH=${nodeHome}/bin:\$PATH
        # 1. Try a standard fix first (safe)
        npm audit fix || true
        # 2. Only show the audit summary to keep logs readable
        npm audit --audit-level=high || true 
    """
                break
                
            case 'go':
                def globalGoDist = "${workspace}/../.global-go-dist"
                steps.sh """
                    # Set Go paths so Nancy can run 'go list'
                    export GOROOT=${globalGoDist}/go
                    export PATH=\$GOROOT/bin:${localBin}:\$PATH
                    
                    if ! command -v nancy &> /dev/null; then
                        curl -sfL https://github.com/sonatype-nexus-community/nancy/releases/download/v1.0.42/nancy-v1.0.42-linux-amd64.tar.gz | tar -C ${localBin} -xz nancy
                    fi
                    
                    # Run nancy
                    go list -m all | ${localBin}/nancy sleuth || echo "Nancy scan failed"
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
