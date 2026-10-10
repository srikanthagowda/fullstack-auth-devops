pipeline {
    agent any

    options {
        timestamps()
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

        stage('Verify Docker Images') {
            steps {
                sh 'docker images'
            }
        }
    }

    post {
        success {
            echo 'CI pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs.'
        }
    }
}