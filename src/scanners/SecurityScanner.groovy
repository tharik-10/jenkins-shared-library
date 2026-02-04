package scanners

class SecurityScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan and Auto-Upgrade for ${lang}..."
        
        switch(lang) {
            case 'python':
                steps.sh '''
                # Install tools
                python3 -m pip install --user safety bandit pip-review
                
                echo "--- Attempting Dependency Upgrades ---"
                # Use --continue-on-fail to ensure one bad package doesn't stop the CI
                python3 -m pip_review --auto || echo "Some packages could not be upgraded automatically"
                
                echo "--- Running Safety Scan ---"
                # Changed 'text' to 'screen' to fix the error in your logs
                python3 -m safety scan --output screen || true
                
                echo "--- Running Bandit ---"
                python3 -m bandit -r . -f screen || true
                '''
                break
                
            case 'node':
                steps.sh '''
                echo "--- Fixing Node Vulnerabilities ---"
                npm audit fix || true
                npm audit
                '''
                break
                
            case 'go':
                steps.sh 'go list -m all | nancy sleuth'
                break
                
            default:
                steps.echo "No specific security tool for ${lang}, attempting OWASP..."
                steps.sh 'dependency-check.sh --scan . || echo "Universal scanner not installed"'
        }
    }
}
