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
            # Create a local bin directory to house our 'fake' docker-compose binary
            mkdir -p dev_bin
            
            if ! command -v docker-compose >/dev/null 2>&1; then
                echo "Creating docker-compose wrapper for Docker V2..."
                echo '#!/bin/bash' > dev_bin/docker-compose
                echo 'docker compose "\$@"' >> dev_bin/docker-compose
                chmod +x dev_bin/docker-compose
            fi

            # Add our local dev_bin to the front of the PATH
            export PATH=\$PWD/dev_bin:\$PATH

            echo "Building monolith image using Makefile..."
            # Now when 'make' calls 'docker-compose', it finds our wrapper script
            make build-images

            # 3. Tag and Push
            echo "Tagging and Pushing image..."
            docker tag ot-microservices:latest ${fullImageName}
            docker push ${fullImageName}
            
            echo "✅ Successfully pushed ${fullImageName}"
            
            # Cleanup wrapper
            rm -rf dev_bin
        """
    }
}
