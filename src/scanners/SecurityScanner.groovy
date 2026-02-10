package scanners

class SecurityScanner implements Serializable {

    static void run(def steps, String lang) {
        steps.echo "Running Security Scan for ${lang}..."

        def workspace = steps.pwd()
        def localBin  = "${workspace}/bin"

        steps.sh "mkdir -p ${localBin}"

        switch (lang) {

            /* =========================
               Python security scanning
               ========================= */
            case 'python':
                steps.sh '''
                    set -e

                    export PATH="$PATH:$HOME/.local/bin"

                    echo "🔧 Installing security tools..."
                    python3 -m pip install --user --upgrade pip setuptools wheel
                    python3 -m pip install --user pip-audit bandit || true

                    echo "🔐 Attempting to auto-fix vulnerable dependencies..."
                    pip-audit --fix || true

                    echo "📊 Running post-fix vulnerability report..."
                    pip-audit || true

                    echo "🛡 Running Bandit source security scan..."
                    python3 -m bandit -r . -x ./venv -f screen || true
                '''
                break


            /* =========================
               Node.js security scanning
               ========================= */
            case 'node':
                def nodeHome = steps.tool name: 'NodeJS-20', type: 'nodejs'

                steps.sh """
                    set -e
                    export PATH=${nodeHome}/bin:\$PATH

                    echo "🔐 Running npm audit auto-fix (safe mode)..."
                    npm audit fix --package-lock-only || true

                    echo "📊 Reporting remaining high/critical vulnerabilities..."
                    npm audit --audit-level=high || true
                """
                break


            /* =========================
               Go security scanning
               ========================= */
            case 'go':
                def globalGoDist = "${workspace}/../.global-go-dist"

                steps.withEnv([
                    "LOCAL_BIN=${localBin}",
                    "GLOBAL_GO=${globalGoDist}"
                ]) {
                    steps.withCredentials([
                        usernamePassword(
                            credentialsId: 'ossindex-creds',
                            usernameVariable: 'OSS_USER',
                            passwordVariable: 'OSS_TOKEN'
                        )
                    ]) {
                        steps.sh '''
                            set -e

                            export GOROOT="$GLOBAL_GO/go"
                            export PATH="$GOROOT/bin:$LOCAL_BIN:$PATH"

                            if [ ! -f "$LOCAL_BIN/nancy" ]; then
                                echo "⬇️ Installing Nancy..."
                                curl -sfL https://github.com/sonatype-nexus-community/nancy/releases/download/v1.0.42/nancy-v1.0.42-linux-amd64.tar.gz \
                                  | tar -C "$LOCAL_BIN" -xz nancy
                            fi

                            echo "🔐 Running Nancy dependency vulnerability scan..."
                            go list -m all | "$LOCAL_BIN/nancy" sleuth \
                              --username "$OSS_USER" \
                              --token "$OSS_TOKEN" \
                              || echo "⚠️ Nancy scan failed"
                        '''
                    }
                }
                break


            /* =========================
               Java security scanning
               ========================= */
            case 'java':
                def mvnHome = steps.tool name: 'maven-3', type: 'maven'

                steps.sh """
                    set -e
                    export PATH=${mvnHome}/bin:\$PATH

                    echo "🧹 Purging old OWASP dependency-check data..."
                    mvn org.owasp:dependency-check-maven:purge -DfailOnError=false || true

                    echo "🔐 Running OWASP dependency-check..."
                    mvn org.owasp:dependency-check-maven:check \
                        -DnvdApiDelay=16000 \
                        -DfailOnError=false \
                        || echo "⚠️ Java dependency scan failed"
                """
                break


            /* =========================
               Default / fallback
               ========================= */
            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}
