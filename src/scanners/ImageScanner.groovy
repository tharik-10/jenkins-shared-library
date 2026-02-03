package scanners

class ImageScanner {
    static void scan(String ecrUrl, String app, String tag) {
        sh """
        trivy image ${ecrUrl}/${app}:${tag} \
        --severity HIGH,CRITICAL \
        --exit-code 1
        """
    }
}
