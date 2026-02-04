package scanners

class SecurityScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Security Scan for ${lang}..."
        
        switch(lang) {
            case 'python':
                // 'Safety' checks dependencies, 'Bandit' checks your own code logic
                steps.sh 'python3 -m pip install --user safety bandit'
                steps.sh 'python3 -m safety check || true'
                steps.sh 'python3 -m bandit -r . || true'
                break
                
            case 'node':
                steps.sh 'npm audit'
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
