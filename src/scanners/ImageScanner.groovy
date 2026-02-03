package scanners

class ImageScanner {
    static void scan(def steps, String ecr, String app, String tag) {
        steps.echo "Scanning Docker Image: ${app}:${tag}"
        steps.sh """
        trivy image ${ecr}/${app}:${tag} \
        --severity HIGH,CRITICAL \
        --exit-code 1
        """
    }
}
