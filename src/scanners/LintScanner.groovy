package scanners

class LintScanner {
    // 1. Remove 'static' (it's cleaner to pass context to a constructor or method)
    // 2. Add 'steps' as a parameter
    static void run(def steps, String lang) {
        switch(lang) {
            case 'python':
                // Use 'steps.sh' instead of just 'sh'
                steps.sh '''
                python3 -m pip install --user flake8
                /var/lib/jenkins/.local/bin/flake8 . || python3 -m flake8 .
                '''
                break
            case 'go':
                steps.sh 'golangci-lint run'
                break
            case 'java':
                steps.sh 'mvn checkstyle:check'
                break
            case 'node':
                steps.sh 'npm run lint'
                break
            default:
                steps.error "Unsupported language: ${lang}"
        }
    }
}
