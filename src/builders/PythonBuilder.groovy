package builders

class PythonBuilder {
    static void build(def steps) {
        steps.sh '''
        python3 -m venv venv
        ./venv/bin/pip install --upgrade pip
        ./venv/bin/pip install -r requirements.txt
        '''
    }
}
