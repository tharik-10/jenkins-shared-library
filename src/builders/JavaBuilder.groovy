package builders

class JavaBuilder {
    static void build(def steps) {
        steps.sh 'mvn clean package -DskipTests'
    }
}
