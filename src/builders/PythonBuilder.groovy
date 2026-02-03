package builders

class PythonBuilder {

    static void build(def script) {
        script.sh '''
          python3 -m venv venv
          source venv/bin/activate
          pip install -r requirements.txt
        '''
    }
}
