package docker

class DockerBuild {
    static void buildAndPush(String ecrUrl, String app, String tag) {
        sh """
        docker build -t ${app}:${tag} .
        docker tag ${app}:${tag} ${ecrUrl}/${app}:${tag}
        docker push ${ecrUrl}/${app}:${tag}
        """
    }
}

