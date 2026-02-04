package scanners

class ImageScanner {
    static void scan(def steps, String ecr, String app, String tag) {
        steps.echo "Scanning Docker Image: ${app}:${tag}"
        
        // Added --ignore-unfixed to bypass vulnerabilities that have no fix yet
        // and --no-progress to keep the Jenkins logs clean.
        steps.sh """
            trivy image ${ecr}/${app}:${tag} \
            --severity HIGH,CRITICAL \
            --ignore-unfixed \
            --no-progress \
            --exit-code 1
        """
    }
}
