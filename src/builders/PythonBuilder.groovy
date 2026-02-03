package builders

class PythonBuilder {
    static void build() {
        // We use 'python3 -m venv' which is the modern standard
        sh '''
        python3 -m venv venv
        ./venv/bin/pip install --upgrade pip
        ./venv/bin/pip install -r requirements.txt
        '''
    }
}
