package docker

class DockerBuild {
    static void buildAndPush(def steps, String ecr, String app, String tag) {
        steps.echo "Building and Pushing Docker image to ECR..."
        steps.sh """
        docker build -t ${app}:${tag} .
        docker tag ${app}:${tag} ${ecr}/${app}:${tag}
        docker push ${ecr}/${app}:${tag}
        """
    }
}
