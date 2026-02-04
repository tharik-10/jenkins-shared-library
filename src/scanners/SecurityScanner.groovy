package scanners

class SecurityScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan and Auto-Upgrade for ${lang}..."
        
        switch(lang) {
            case 'python':
                steps.sh '''
                # Install security tools and the upgrade manager
                python3 -m pip install --user safety bandit pip-review
                
                # Automatically upgrade all dependencies to latest stable versions
                echo "--- Upgrading Python Dependencies ---"
                python3 -m pip_review --auto
                
                # Run the scan (using '|| true' so we can see reports even if risks exist)
                echo "--- Running Safety Scan ---"
                python3 -m safety scan --output text || true
                
                echo "--- Running Bandit Static Analysis ---"
                python3 -m bandit -r . -f txt || true
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
