def buildJar() {
    echo 'Building the application ...'
    sh 'mvn package'
}

def buildImage() {
    echo 'Building the Docker image ...'
    withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh 'docker build -t alikakavand/demo-app:jma-2.0 .'
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh 'docker push alikakavand/demo-app:jma-2.0'
    }
}

def deployApp() {
    echo 'Deploying the application ...'
}

return this