package builders

class NodeBuilder {
    static void build(def steps) {
        steps.sh '''
        npm ci
        npm run build
        '''
    }
}
