package builders

class NodeBuilder {
    static void build() {
        sh '''
        npm ci
        npm run build
        '''
    }
}
