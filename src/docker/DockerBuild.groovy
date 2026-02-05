package docker

class DockerBuild {
    static void buildAndPush(def steps, String ecrUrl, String appName, String tag) {
        String region = "us-east-1"
        String fullImageName = "${ecrUrl}/${appName}:${tag}"
        // Define the local name that your Makefile actually produces
        String localImageName = "ot-microservices-ci-empms-frontend" 
        
        // 1. Ensure ECR Repo exists
        steps.sh """
            aws ecr describe-repositories --repository-names ${appName} --region ${region} || \
            aws ecr create-repository --repository-name ${appName} --region ${region}
        """

        // 2. Login to ECR
        steps.sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrUrl}"
        
        steps.echo "🚀 Starting Monolith Build via Docker Compose Wrapper..."
        
        steps.sh """
            mkdir -p dev_bin
            if ! command -v docker-compose >/dev/null 2>&1; then
                echo '#!/bin/bash' > dev_bin/docker-compose
                echo 'docker compose "\$@"' >> dev_bin/docker-compose
                chmod +x dev_bin/docker-compose
            fi

            export PATH=\$PWD/dev_bin:\$PATH

            echo "Running Makefile build-images..."
            make build-images

            # Use the variable defined at the top of the method
            docker tag ${localImageName}:latest ${fullImageName}
            docker push ${fullImageName}
            
            rm -rf dev_bin
        """
    }
}
