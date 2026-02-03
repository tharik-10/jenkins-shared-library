package scanners

class ImageScanner {
    static void scan(String app, String tag) {
        sh """
        trivy image ${ECR_URL}/${app}:${tag} \
        --severity HIGH,CRITICAL \
        --exit-code 1
        """
    }
}
