package docker

class DockerBuild {
    static void buildAndPush(def steps, String ecrUrl, String appName, String tag) {
        String region = "ap-south-1"
        
        steps.echo "Checking if ECR repository ${appName} exists..."
        
        // This command checks for the repo; if it fails, it creates it.
        steps.sh """
            aws ecr describe-repositories --repository-names ${appName} --region ${region} || \
            aws ecr create-repository --repository-name ${appName} --region ${region}
        """

        steps.echo "Logging into Amazon ECR..."
        steps.sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrUrl}"

        steps.echo "Building and Pushing Docker image..."
        String fullImageName = "${ecrUrl}/${appName}:${tag}"
        
        steps.sh """
            docker build -t ${appName}:${tag} .
            docker tag ${appName}:${tag} ${fullImageName}
            docker push ${fullImageName}
        """
    }
}
