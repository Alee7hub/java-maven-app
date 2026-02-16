pipeline {
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        stage('build jar') {
            steps {
                echo 'Building the application ...'
                sh 'mvn package'
            }
        }
        stage('build image') {
            steps {
                echo 'Building the Docker image ...'
                withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh 'docker build -t alikakavand/demo-app:jma-2.0 .'
                    sh 'echo $PASS | docker login -u $USER --password-stdin'
                    sh 'docker push alikakavand/demo-app:jma-2.0'
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying the application ...'
            }
        }
    }
}