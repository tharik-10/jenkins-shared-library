package builders

class GoBuilder {
    static void build() {
        script.sh '''
          go mod download
          go build ./...
        '''
    }
}
