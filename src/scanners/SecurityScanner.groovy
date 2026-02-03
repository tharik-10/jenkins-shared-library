package scanners

class SecurityScanner {
    static void run(def steps) {
        steps.echo "Running Dependency Security Scan..."
        // Adjust this path to where your dependency-check tool is installed
        steps.sh 'dependency-check.sh --scan . --failOnCVSS 7'
    }
}
