package builders

class NodeBuilder {
    static void build() {
        script.sh '''
          npm install
          npm run build
        '''
    }
}
