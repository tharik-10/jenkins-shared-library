package builders

class NodeBuilder {
    static void build() {
        sh '''
        npm install
        npm run build
        '''
    }
}
