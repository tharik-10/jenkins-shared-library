package scanners

class LintScanner {
    static void run(def steps, String lang) {
        switch(lang) {
            case 'python':
                // Using 'python3 -m' ensures we use the module we just installed
                // even if the standalone 'flake8' command isn't in the PATH
                sh '''
                python3 -m pip install --user flake8
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
