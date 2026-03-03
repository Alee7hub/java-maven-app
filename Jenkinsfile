#!/usr/bin/env groovy

library identifier: 'jenkins-shared-library@main', retriever: modernSCM(
    [$class: 'GitSCMSource',
    remote: 'https://github.com/Alee7hub/jenkins-shared-library.git',
    credentialsId: 'github-pat'
    ]
)

pipeline {
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        stage('increment version') {
            steps {
                script {
                    echo 'incrementing app version...'
                    sh 'mvn build-helper:parse-version versions:set \
                        -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
                        versions:commit'
                    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
                    def version = matcher[0][1]
                    env.IMAGE_NAME = "alikakavand/demo-app:$version-$BUILD_NUMBER"
                }
            }
        }
        stage('build app') {
            steps {
                echo 'building application jar...'
                buildJar()
            }
        }
        stage('build image') {
            steps {
                script {
                    echo 'building the docker image...'
                    buildImage(env.IMAGE_NAME)
                    dockerLogin()
                    dockerPush(env.IMAGE_NAME)
                }
            }
        } 
        stage("deploy") {
            steps {
                script {
                    echo 'deploying docker image to EC2...'

                    def ec2Instance = "ec2-user@i-08fb1bc876cd3897b"
                    def instanceId = "i-08fb1bc876cd3897b"

                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                    credentialsId: 'aws-jenkins-key',
                                    accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                    secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {

                        // Copy files from Jenkins to EC2 using scp tunneled through SSM
                        sshagent(['ec2-server-key']) {
                            sh """
                                scp -O -o StrictHostKeyChecking=no \
                                -o "ProxyCommand=sh -c 'aws ssm start-session --target ${instanceId} --document-name AWS-StartSSHSession --parameters portNumber=%p --region eu-central-1'" \
                                server-cmds.sh ${ec2Instance}:/home/ec2-user
                            """
                            sh """
                                scp -O -o StrictHostKeyChecking=no \
                                -o "ProxyCommand=sh -c 'aws ssm start-session --target ${instanceId} --document-name AWS-StartSSHSession --parameters portNumber=%p --region eu-central-1'" \
                                docker-compose.yaml ${ec2Instance}:/home/ec2-user
                            """
                        }

                        // Run the script on EC2 via SSM and wait for result
                        sh """
                            COMMAND_ID=\$(aws ssm send-command \
                            --instance-ids i-08fb1bc876cd3897b \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["bash /home/ec2-user/server-cmds.sh ${env.IMAGE_NAME}"]' \
                            --region eu-central-1 \
                            --query 'Command.CommandId' \
                            --output text)

                            echo "SSM Command ID: \$COMMAND_ID"

                            aws ssm wait command-executed \
                            --command-id \$COMMAND_ID \
                            --instance-id i-08fb1bc876cd3897b \
                            --region eu-central-1

                            aws ssm get-command-invocation \
                            --command-id \$COMMAND_ID \
                            --instance-id i-08fb1bc876cd3897b \
                            --region eu-central-1 \
                            --query 'StandardOutputContent' \
                            --output text
                        """
                    }
                }
            }
        }
        stage('commit version update'){
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'github-pat', passwordVariable: 'PASS', usernameVariable: 'USER')]){
                        sh 'git config user.name "Jenkins CI"'
                        sh 'git config user.email "jenkins@example.com"'
                        sh 'git remote set-url origin https://$USER:$PASS@github.com/Alee7hub/java-maven-app.git'
                        sh 'git clean -fd target/ || true'
                        sh 'git rebase --abort || rm -fr .git/rebase-merge || true'
                        sh 'git add pom.xml'
                        sh 'git diff --cached --quiet || git commit -m "ci: version bump"'
                        sh 'git checkout -- .'
                        sh 'git pull --rebase origin jenkins-jobs'
                        sh 'git push origin HEAD:jenkins-jobs'
                    }
                }
            }
        }
    }
}
