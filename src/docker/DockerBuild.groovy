package docker

class DockerBuild {
    static void buildAndPush(def steps, String ecrUrl, String appName, String tag) {
        String region = "us-east-1"
        String fullImageName = "${ecrUrl}/${appName}:${tag}"
        
        // 1. Ensure ECR Repo exists
        steps.sh """
            aws ecr describe-repositories --repository-names ${appName} --region ${region} || \
            aws ecr create-repository --repository-name ${appName} --region ${region}
        """

        // 2. Login to ECR
        steps.sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrUrl}"
        
        steps.echo "🚀 Starting Monolith Build via Docker Compose..."
        
        steps.sh """
            # Check if docker-compose is installed, if not, try to use 'docker compose' (V2)
            if command -v docker-compose >/dev/null 2>&1; then
                DOCKER_COMPOSE_CMD="docker-compose"
            else
                DOCKER_COMPOSE_CMD="docker compose"
            fi

            echo "Using command: \$DOCKER_COMPOSE_CMD"

            # Build images using the Makefile (which calls compose)
            # If your Makefile is hardcoded to 'docker-compose', we alias it:
            alias docker-compose='docker compose' 2>/dev/null || true
            
            make build-images

            # 3. Tag and Push
            # Assuming the Makefile produces an image named 'ot-microservices:latest'
            docker tag ot-microservices:latest ${fullImageName}
            docker push ${fullImageName}
            
            echo "✅ Successfully pushed ${fullImageName}"
        """
    }
}
