package scanners

class LintScanner {

    static void run(def script, String language) {

        switch(language) {
            case 'python':
                script.sh 'pip install flake8 && flake8 .'
                break

            case 'go':
                script.sh 'golangci-lint run'
                break

            case 'java':
                script.sh 'mvn checkstyle:check'
                break

            case 'node':
                script.sh 'npm install && npm run lint'
                break
        }
    }
}
