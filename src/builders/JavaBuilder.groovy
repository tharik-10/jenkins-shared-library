package builders

class JavaBuilder {
    static void build() {
        sh 'mvn clean package -DskipTests'
    }
}
