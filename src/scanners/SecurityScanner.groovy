package scanners

class SecurityScanner {
    static void run() {
        sh '''
        dependency-check.sh --scan . --failOnCVSS 7
        '''
    }
}
