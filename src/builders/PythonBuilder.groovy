package builders

class PythonBuilder {
    static void build() {
        sh '''
        python3 -m venv venv
        source venv/bin/activate
        pip install -r requirements.txt
        '''
    }
}
