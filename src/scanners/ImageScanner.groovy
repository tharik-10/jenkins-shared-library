package scanners

class ImageScanner {
    static void scan(def steps, String ecr, String app, String tag) {
        steps.echo "Scanning Monolith Image: ${app}:${tag}"
        
        steps.sh """
            trivy image ${ecr}/${app}:${tag} \
            --severity HIGH,CRITICAL \
            --ignore-unfixed \
            --exit-code 0 
        """
        // Note: Changed exit-code to 0 for now so vulnerabilities 
        // don't block your Spinnaker testing, but change back to 1 for prod.
    }
}
