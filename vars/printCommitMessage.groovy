def call() {
    def message = sh(script: "git log -1 --pretty=format:%B", returnStdout: true).trim()
    echo "📝 Latest Commit Message:\n${message}"
}
