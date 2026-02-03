package builders

class GoBuilder {
    static void build() {
        sh 'go build ./...'
    }
}
