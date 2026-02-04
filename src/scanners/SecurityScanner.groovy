package scanners

class SecurityScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan and Auto-Upgrade for ${lang}..."
        
        switch(lang) {
            case 'python':
                steps.sh '''
                python3 -m pip install --user safety bandit
                if [ -f requirements.txt ]; then
                    python3 -m pip install --user --upgrade -r requirements.txt || echo "Upgrades failed"
                fi
                python3 -m safety check --full-report || true
                python3 -m bandit -r . -f screen || true
                '''
                break
                
            case 'node':
                steps.sh '''
                npm audit fix || true
                npm audit
                '''
                break
                
            case 'go':
                steps.sh 'go list -m all | nancy sleuth'
                break

            /* ==========================
               ADDED JAVA LOGIC
               ========================== */
            case 'java':
                steps.echo "--- Running Maven Dependency Check ---"
                // This uses the Maven plugin to check for CVEs. 
                // If you use Gradle, change this to './gradlew dependencyCheckAnalyze'
                steps.sh 'mvn org.owasp:dependency-check-maven:check || echo "Dependency check failed"'
                break
                
            default:
                steps.echo "No specific security tool for ${lang}, skipping scan."
        }
    }
}
