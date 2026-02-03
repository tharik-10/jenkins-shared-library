package builders

class NodeBuilder {
    static void build() {
        script.sh '''
        npm ci
        npm run build
        '''
    }
}
