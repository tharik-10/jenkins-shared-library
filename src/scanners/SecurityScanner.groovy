package scanners

class SecurityScanner {
    static void run() {
        sh '''
        mvn sonar:sonar || true
        dependency-check.sh --scan . --failOnCVSS 7
        '''
    }
}
