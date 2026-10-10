pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        DOCKERHUB_USER = 'srikanthagowda098'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Source code checked out successfully'
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
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

                        docker tag fullstack-backend:${BUILD_NUMBER} "$DOCKERHUB_USERNAME/fullstack-backend:${BUILD_NUMBER}"
                        docker tag fullstack-frontend:${BUILD_NUMBER} "$DOCKERHUB_USERNAME/fullstack-frontend:${BUILD_NUMBER}"

                        docker push "$DOCKERHUB_USERNAME/fullstack-backend:${BUILD_NUMBER}"
                        docker push "$DOCKERHUB_USERNAME/fullstack-frontend:${BUILD_NUMBER}"

                        docker logout
                    '''
                }
            }
        }

        stage('Verify Docker Images') {
            steps {
                sh 'docker images'
            }
        }
    }

    post {
        success {
            echo 'CI pipeline and Docker Hub publishing completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs.'
        }
    }
}
