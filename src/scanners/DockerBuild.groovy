package docker

class DockerBuild {
    static void buildAndPush(String app, String tag) {
        sh """
        docker build -t ${app}:${tag} .
        docker tag ${app}:${tag} ${ECR_URL}/${app}:${tag}
        docker push ${ECR_URL}/${app}:${tag}
        """
    }
}
