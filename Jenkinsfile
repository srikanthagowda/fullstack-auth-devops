pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        DOCKERHUB_USER = 'srikanthagowda098'
        PRODUCTION_HOST = '172.31.11.146'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Source code checked out successfully'
            }
        }

        stage('Frontend Quality Checks') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run lint'
                    sh 'npm run build'
                }
            }
        }

        stage('Test and Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn -B clean test package'
                }
            }
        }

        stage('Build Backend Docker Image') {
            steps {
                sh 'docker build -t fullstack-backend:${BUILD_NUMBER} ./backend'
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                sh 'docker build -t fullstack-frontend:${BUILD_NUMBER} ./frontend'
            }
        }

        stage('Push Images to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x
                        echo "$DOCKERHUB_TOKEN" | docker login \
                            -u "$DOCKERHUB_USERNAME" --password-stdin

                        docker tag fullstack-backend:${BUILD_NUMBER} \
                            "$DOCKERHUB_USERNAME/fullstack-backend:${BUILD_NUMBER}"

                        docker tag fullstack-frontend:${BUILD_NUMBER} \
                            "$DOCKERHUB_USERNAME/fullstack-frontend:${BUILD_NUMBER}"

                        docker push \
                            "$DOCKERHUB_USERNAME/fullstack-backend:${BUILD_NUMBER}"

                        docker push \
                            "$DOCKERHUB_USERNAME/fullstack-frontend:${BUILD_NUMBER}"

                        docker logout
                    '''
                }
            }
        }

        stage('Verify Docker Images') {
            steps {
                sh '''
                    docker image inspect \
                        "$DOCKERHUB_USER/fullstack-backend:${BUILD_NUMBER}"
                    docker image inspect \
                        "$DOCKERHUB_USER/fullstack-frontend:${BUILD_NUMBER}"
                '''
            }
        }

        stage('Test Production SSH') {
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'production-ec2-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh '''
                        set +x
                        ssh -i "$SSH_KEY" \
                            -o BatchMode=yes \
                            -o StrictHostKeyChecking=accept-new \
                            -o ConnectTimeout=10 \
                            "$SSH_USER@$PRODUCTION_HOST" \
                            'hostname && whoami'
                    '''
                }
            }
        }

        stage('Deploy to Production') {
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'production-ec2-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh '''
                        set +x

                        ssh -i "$SSH_KEY" \
                            -o BatchMode=yes \
                            -o StrictHostKeyChecking=accept-new \
                            -o ConnectTimeout=10 \
                            "$SSH_USER@$PRODUCTION_HOST" \
                            "bash -s -- '$BUILD_NUMBER' '$DOCKERHUB_USER'" <<'REMOTE'
set -eu

BUILD_NUMBER="$1"
DOCKERHUB_USER="$2"

BACKEND_IMAGE="${DOCKERHUB_USER}/fullstack-backend:${BUILD_NUMBER}"
FRONTEND_IMAGE="${DOCKERHUB_USER}/fullstack-frontend:${BUILD_NUMBER}"

docker_cmd() {
    sudo -n docker "$@"
}

echo "Checking existing production containers..."

docker_cmd inspect fullstack-backend >/dev/null
docker_cmd inspect fullstack-frontend >/dev/null
docker_cmd inspect mysql-db >/dev/null
docker_cmd network inspect fullstack-network >/dev/null

echo "Pulling application images..."
docker_cmd pull "$BACKEND_IMAGE"
docker_cmd pull "$FRONTEND_IMAGE"

# Preserve DB settings without printing their values.
ENV_FILE=$(mktemp)
chmod 600 "$ENV_FILE"
trap 'rm -f "$ENV_FILE"' EXIT

docker_cmd inspect fullstack-backend 
    --format '{{range .Config.Env}}{{println .}}{{end}}' 
    | grep -E '^(DB_URL|DB_USERNAME|DB_PASSWORD)=' > "$ENV_FILE"

for variable in DB_URL DB_USERNAME DB_PASSWORD; do
    if ! grep -q "^${variable}=" "$ENV_FILE"; then
        echo "ERROR: Required database setting missing: ${variable}"
        exit 1
    fi
done

echo "Replacing backend container..."
docker_cmd rm -f fullstack-backend

docker_cmd run -d 
    --name fullstack-backend 
    --restart unless-stopped 
    --network fullstack-network 
    --network-alias backend 
    --env-file "$ENV_FILE" 
    -p 8085:8085 
    "$BACKEND_IMAGE"

echo "Replacing frontend container..."
docker_cmd rm -f fullstack-frontend

docker_cmd run -d 
    --name fullstack-frontend 
    --restart unless-stopped 
    --network fullstack-network 
    -p 8081:80 
    "$FRONTEND_IMAGE"

echo "Checking container status..."
docker_cmd ps --filter name=fullstack-backend
docker_cmd ps --filter name=fullstack-frontend

echo "Deployment commands completed."
REMOTE
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'CI, Docker Hub publishing, SSH verification, and deployment completed.'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs before retrying.'
        }
        always {
            echo 'Pipeline execution finished.'
        }
    }
}