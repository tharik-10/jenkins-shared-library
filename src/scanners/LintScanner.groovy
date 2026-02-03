package scanners

class LintScanner {
    static void run(String lang) {
        switch(lang) {
            case 'python':
                sh 'pip install flake8 && flake8 .'
                break
            case 'go':
                sh 'golangci-lint run'
                break
            case 'java':
                sh 'mvn checkstyle:check'
                break
            case 'node':
                sh 'npm ci && npm run lint'
                break
            default:
                error "Unsupported language: ${lang}"
        }
    }
}
