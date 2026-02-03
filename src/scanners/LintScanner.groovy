package scanners

class LintScanner {
    static void run(def steps, String lang) {
        steps.echo "Running Linting for ${lang}..."
        switch(lang) {
            case 'python':
                steps.sh 'python3 -m pip install --user flake8 && python3 -m flake8 . --ignore=E501'
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
