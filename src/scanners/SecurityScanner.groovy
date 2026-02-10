package scanners

class SecurityScanner implements Serializable {

    static void run(def steps, String lang) {
        steps.echo "Running Security Scan for ${lang}..."
        def workspace = steps.pwd()
        def localBin = "${workspace}/bin"

        steps.sh "mkdir -p ${localBin}"

        switch (lang) {

            case 'python':
                steps.sh """
                    export PATH=\$PATH:\$HOME/.local/bin

                    echo "🔧 Installing security tools..."
                    python3 -m pip install --user --upgrade pip setuptools wheel
                    python3 -m pip install --user pip-audit bandit || true

                    echo "🔐 Attempting to auto-fix vulnerable dependencies..."
                    pip-audit --fix || true

                    echo "📊 Running post-fix vulnerability report..."
                    pip-audit || true

                    echo "🛡 Running Bandit source security scan..."
                    python3 -m bandit -r . -x ./venv -f screen || true
                """
                break

            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'
                steps.sh """
                    export PATH=${nodeHome}/bin:\$PATH

                    echo "🔐 Running npm audit auto-fix (safe mode)..."
                    npm audit fix --package-lock-only || true

                    echo "📊 Reporting remaining high/critical vulnerabilities..."
                    npm audit --audit-level=high || true
                """
                break

            case 'go':
                def globalGoDist = "${workspace}/../.global-go-dist"

                steps.withCredentials([
                    steps.usernamePassword(
                        credentialsId: 'ossindex-creds',
                        usernameVariable: 'OSS_USER',
                        passwordVariable: 'OSS_TOKEN'
                    )
                ]) {
                    steps.sh """
                        export GOROOT=${globalGoDist}/go
                        export PATH=\$GOROOT/bin:${localBin}:\$PATH

                        if [ ! -f "${localBin}/nancy" ]; then
                            echo "⬇️ Installing Nancy..."
                            curl -sfL https://github.com/sonatype-nexus-community/nancy/releases/download/v1.0.42/nancy-v1.0.42-linux-amd64.tar.gz \
                            | tar -C ${localBin} -xz nancy
                        fi

                        echo "🔎 Running Nancy OSS Index scan..."
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
                    mvn org.owasp:dependency-check-maven:check \
                        -DnvdApiDelay=16000 \
                        -DfailOnError=false \
                        || echo "Java scan failed"
                """
                break

            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}

