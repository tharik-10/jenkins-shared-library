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
        
        steps.echo "🚀 Starting Monolith Build via Docker Compose Wrapper..."
        
        steps.sh """
            # Create a temporary bin folder to act as a bridge for the Makefile
            mkdir -p dev_bin
            
            if ! command -v docker-compose >/dev/null 2>&1; then
                echo "Mapping 'docker-compose' to 'docker compose'..."
                echo '#!/bin/bash' > dev_bin/docker-compose
                echo 'docker compose "\$@"' >> dev_bin/docker-compose
                chmod +x dev_bin/docker-compose
            fi

            # Add the wrapper to the PATH so 'make' finds it
            export PATH=\$PWD/dev_bin:\$PATH

            echo "Running Makefile build-images..."
            make build-images

            # 3. Tag and Push the image created by the Makefile
            # Note: Ensure 'ot-microservices' matches the image name in your Makefile
            docker tag ${localImageName}:latest ${fullImageName}
            docker push ${fullImageName}
            
            echo "✅ Successfully pushed ${fullImageName}"
            
            # Clean up
            rm -rf dev_bin
        """
    }
}
