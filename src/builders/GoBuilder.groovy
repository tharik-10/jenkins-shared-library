package builders

class GoBuilder {
    static void build(def steps) {
        steps.sh '''
        go mod download
        go build ./...
        '''
    }
}
