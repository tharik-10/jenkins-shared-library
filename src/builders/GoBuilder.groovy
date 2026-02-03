package builders

class GoBuilder {
    static void build() {
        sh '''
        go mod download
        go build ./...
        '''
    }
}
