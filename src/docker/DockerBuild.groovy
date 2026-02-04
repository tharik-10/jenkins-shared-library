package docker

class DockerBuild {
    static void buildAndPush(def steps, String ecrUrl, String appName, String tag) {
        String region = "us-east-1"
        
        // Ensure the Monolith repository exists in ECR
        steps.sh """
            aws ecr describe-repositories --repository-names ${appName} --region ${region} || \
            aws ecr create-repository --repository-name ${appName} --region ${region}
        """

        steps.sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrUrl}"

        String fullImageName = "${ecrUrl}/${appName}:${tag}"
        
        steps.echo "Building monolith image using Makefile..."
        steps.sh """
            # Using your existing Makefile logic
            make build-images
            
            # Tag the resulting monolith image for ECR
            # (Assuming the make command creates an image named 'ot-microservices')
            docker tag ot-microservices:latest ${fullImageName}
            docker push ${fullImageName}
        """
    }
}
