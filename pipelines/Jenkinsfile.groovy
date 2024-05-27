#!groovy

import groovy.json.Json.JsonSlurperClassic

// SCM data is no longer provided in environment variables when
// calling "checkout scm", it is now the return value of the call
def scmData = null
def logDir = "logs"

// This is a workaround for a bug in Jenkins - JENKINS-40574
// On the first run of the pipeline default parameters will not be set
def getLabel() {
  def label = params.MACHINE
  if (label == null || label == 'default') {
    label = any
  }
  println "PIPELINE: getLabel: MACHINE parameter is ${params.MACHINE}, Setting the label to ${label}"
  return label
}

pipeline {

    parameters {
        string(name: 'BRANCH', defaultValue: 'develop', description: 'The branch to build')
    }

    options {
        timeout(time: 90, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '14', numToKeepStr: '10'))
        skipDefaultCheckout true
    }

    agent {
        label getLabel()
    }

    environment {
        DOCKER_IMAGE = "your-docker-image"
        AWS_REGION = "your-aws-region"
        CLUSTER_NAME = "your-cluster-name"
        NAMESPACE = "your-namespace"
        DEPLOYMENT_NAME = "your-deployment-name"
        APP_REPO_URL = 'https://github.com/your-app-repo.git'
        APP_BRANCH = 'main'
        SLACK_CHANNEL = "#your-slack-channel"
        SLACK_CREDENTIAL_ID = "your-slack-credential-id"
    }

    stages {

        stage('Checkout CICD') {
            steps {
                echo "${STAGE_NAME} Stage Execution Starting"
                checkout([
                    changelog: false,
                    poll: false,
                    scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/develop']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'RelativeTargetDirectory', relativeTargetDir: 'CloudBash-CICD']
                        ],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            credentialsId: 'da701aad-9139-4a8c-b2b9-32f5872d9de3',
                            url: 'https://github.com/DevSecOps-Project/CloudBash-CICD.git'
                        ]]
                    ]
                ])
                // This is a workaround for a bug in Jenkins - JENKINS-47801
                // Cannot stash an empty dir therefore we are creating a empty dummy file inside the log dir 
                dir(logDir){
                writeFile file: 'dummy.txt', text: ""
                }
            }
            post {
                success {
                    echo "${STAGE_NAME} Stage Finished Successfully"
                }
                failure {
                    echo "${STAGE_NAME} Stage Failed"
                }
            }
        }

        stage('Checkout App') {
            steps {
                echo "${STAGE_NAME} Stage Execution Starting"
                checkout([
                    changelog: false,
                    poll: false,
                    scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/${APP_BRANCH}']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'RelativeTargetDirectory', relativeTargetDir: 'CloudBash-CICD']
                        ],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            credentialsId: 'da701aad-9139-4a8c-b2b9-32f5872d9de3',
                            url: 'https://github.com/DevSecOps-Project/CloudBash.git'
                        ]]
                    ]
                ])
                // This is a workaround for a bug in Jenkins - JENKINS-47801
                // Cannot stash an empty dir therefore we are creating a empty dummy file inside the log dir 
                dir(logDir){
                writeFile file: 'dummy.txt', text: ""
                }
            }
            post {
                success {
                    echo "${STAGE_NAME} Stage Finished Successfully"
                }
                failure {
                    echo "${STAGE_NAME} Stage Failed"
                }
            }
        }

    //     stage('Build Docker Image') {
    //         steps {
    //             script {
    //                 dockerImage = docker.build("${DOCKER_IMAGE}:${env.BUILD_NUMBER}", 'app/.')
    //             }
    //         }
    //     }

    //     stage('Lint') {
    //         steps {
    //             dir('app') {
    //                 sh 'npm run lint'
    //             }
    //         }
    //     }

    //     stage('Unit Tests') {
    //         steps {
    //             dir('app') {
    //                 sh 'npm test'
    //             }
    //         }
    //     }

    //     stage('Push Docker Image') {
    //         steps {
    //             script {
    //                 docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
    //                     dockerImage.push("${env.BUILD_NUMBER}")
    //                     dockerImage.push("latest")
    //                 }
    //             }
    //         }
    //     }

    //     stage('Deploy to EKS') {
    //         steps {
    //             script {
    //                 sh '''
    //                     aws eks --region ${AWS_REGION} update-kubeconfig --name ${CLUSTER_NAME}
    //                     kubectl set image deployment/${DEPLOYMENT_NAME} ${DEPLOYMENT_NAME}=${DOCKER_IMAGE}:${env.BUILD_NUMBER} --namespace=${NAMESPACE}
    //                 '''
    //             }
    //         }
    //     }
    // }

    // post {
    //     success {
    //         script {
    //             slackSend(
    //                 channel: "${SLACK_CHANNEL}",
    //                 color: "good",
    //                 message: "Build ${env.BUILD_NUMBER} was successful. Deployed to ${CLUSTER_NAME}/${NAMESPACE}"
    //             )
    //         }
    //     }
    //     failure {
    //         script {
    //             slackSend(
    //                 channel: "${SLACK_CHANNEL}",
    //                 color: "danger",
    //                 message: "Build ${env.BUILD_NUMBER} failed. Check Jenkins for details."
    //             )
    //         }
    //     }
    //     always {
    //         cleanWs()
    //     }
    // }
}
