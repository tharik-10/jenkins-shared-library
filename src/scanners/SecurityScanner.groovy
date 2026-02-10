package scanners

class SecurityScanner implements Serializable {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan for ${lang}..."
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"
        steps.sh "mkdir -p ${localBin}"
        
        switch(lang) {
            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin
                    python3 -m pip install --user --upgrade pip setuptools wheel
                    python3 -m pip install --user safety bandit || true
                    
                    echo "--- Running Security Scan ---"
                    python3 -m safety check --full-report || true
                    python3 -m bandit -r . -x ./venv -f screen || true
                """
                break
                
            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
                steps.sh """
                    export PATH=${nodeHome}/bin:\$PATH
                    npm audit fix || true
                    npm audit --audit-level=high || true 
                """
                break
                
            case 'go':
                def globalGoDist = "${workspace}/../.global-go-dist"
    steps.withCredentials([
        usernamePassword(
            credentialsId: 'ossindex-creds',
            usernameVariable: 'OSS_USER',
            passwordVariable: 'OSS_TOKEN'
        )
    ]) {
        steps.sh """
            export GOROOT=${globalGoDist}/go
            export PATH=\$GOROOT/bin:${localBin}:\$PATH

            if [ ! -f "${localBin}/nancy" ]; then
                curl -sfL https://github.com/sonatype-nexus-community/nancy/releases/download/v1.0.42/nancy-v1.0.42-linux-amd64.tar.gz \
                | tar -C ${localBin} -xz nancy
            fi

            go list -m all | ${localBin}/nancy sleuth \
              --username \$OSS_USER \
              --token \$OSS_TOKEN \
              || echo "⚠️ Nancy scan failed"
        """
    }
                break

            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'
                steps.sh """
                    export PATH=${mvnHome}/bin:\$PATH
                    mvn org.owasp:dependency-check-maven:purge -DfailOnError=false || true
                    mvn org.owasp:dependency-check-maven:check -DnvdApiDelay=16000 -DfailOnError=false || echo "Java scan failed"
                """
                break
                
            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}
