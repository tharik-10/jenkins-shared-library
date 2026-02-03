package scanners

class LintScanner {
    static void run(def steps, String lang) {
        switch(lang) {
            case 'python':
                steps.sh '''
                python3 --version
                python3 -m ensurepip || true
                python3 -m pip install --upgrade pip
                python3 -m pip install flake8
                python3 -m flake8 .
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
