package scanners

class ImageScanner {
    static void scan(String app, String tag) {
        sh """
        trivy image ${ECR_URL}/${app}:${tag} --exit-code 1 --severity HIGH,CRITICAL
        """
    }
}
