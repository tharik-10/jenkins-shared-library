package scanners

class LintScanner {
    static void run(def steps, String lang) {
        switch(lang) {
            case 'python':
                steps.sh '''
                pip install flake8
                flake8 .
                '''
                break
            case 'go':
                steps.sh 'golangci-lint run'
                break
            case 'java':
                steps.sh 'mvn checkstyle:check'
                break
            case 'node':
                steps.sh '''
                npm ci
                npm run lint
                '''
                break
        }
    }
}
