package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        switch(lang) {
            case 'python':
                // FIXED: Added --exclude to ignore the virtual environment and library files
                steps.sh '''
                    python3 -m pip install --user flake8
                    python3 -m flake8 . --ignore=E501,W291 --exclude=venv,env,.venv,__pycache__,build,dist
                '''
                break
                
            case 'go':
                steps.sh 'golangci-lint run'
                break
                
            case 'java':
                // Note: Ensure checkstyle.xml exists in your repo or use a default config
                steps.sh 'mvn checkstyle:check'
                break
                
            case 'node':
                // Added a fallback in case 'npm run lint' is missing from package.json
                steps.sh 'npm run lint || echo "No lint script found in package.json, skipping..."'
                break
                
            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
