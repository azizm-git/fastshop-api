@Library('lib') _

pipeline {

    agent { label 'master' }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_NAME   = 'azizmjd/fastshop-api'
        IMAGE_TAG    = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        APP_PORT     = '8080'
        STAGING_HOST = '192.168.170.134'
        STAGING_PORT = '8090'
        PROD_HOST    = '192.168.170.135'
        PROD_PORT    = '8091'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branche : ${env.BRANCH_NAME}  |  Commit : ${env.GIT_COMMIT?.take(8)}"
            }
        }

        stage('Build & Tests Unitaires') {
            steps {
                script { buildMaven() }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    buildDocker(name: env.IMAGE_NAME, tag: env.IMAGE_TAG)
                }
            }
        }

        stage('Push Docker Hub') {
            steps {
                script {
                    pushDocker(
                        name: env.IMAGE_NAME,
                        tag:  env.IMAGE_TAG,
                        credentialsId: 'dockerhub-creds'
                    )
                }
            }
        }

        stage('Deploy Staging') {
            when { branch 'develop' }
            steps {
                script {
                    deployAnsible(
                        host:  env.STAGING_HOST,
                        image: "${env.IMAGE_NAME}:${env.IMAGE_TAG}",
                        env:   'staging'
                    )
                }
            }
        }

        stage('Pre-deploy Staging (for tests)') {
            when { branch 'main' }
            steps {
                script {
                    deployAnsible(
                        host:  env.STAGING_HOST,
                        image: "${env.IMAGE_NAME}:${env.IMAGE_TAG}",
                        env:   'staging'
                    )
                }
            }
        }

        stage('JMeter Performance Gate') {
            when { branch 'main' }
            steps {
                sh """
                    /opt/jmeter/bin/jmeter -n \
                        -t tests/perf/catalogue.jmx \
                        -Jhost=${STAGING_HOST} \
                        -Jport=${STAGING_PORT} \
                        -l results.jtl
                """
                echo "JMeter terminé — résultats dans results.jtl"
                archiveArtifacts artifacts: 'results.jtl', allowEmptyArchive: true
            }
        }

        stage('Approval Production') {
            when { branch 'main' }
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    input(
                        message: "Déployer en PRODUCTION sur ${env.PROD_HOST} ?",
                        ok: 'Déployer'
                    )
                }
            }
        }

        stage('Deploy Production') {
            when { branch 'main' }
            steps {
                script {
                    sh """
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest-prod || true
                    """
                    deployAnsible(
                        host:  env.PROD_HOST,
                        image: "${env.IMAGE_NAME}:${env.IMAGE_TAG}",
                        env:   'production'
                    )
                }
            }
        }

        stage('Smoke Test Production') {
            when { branch 'main' }
            steps {
                sh """
                    sleep 5
                    curl -fsS http://${PROD_HOST}:${PROD_PORT}/api/products/health | grep -q '"status":"UP"' || { echo 'Smoke test KO'; exit 1; }
                    echo 'Smoke test PROD OK'
                """
            }
        }
    }

    post {
        success {
            echo "[${env.JOB_NAME} #${env.BUILD_NUMBER}] branche ${env.BRANCH_NAME} - SUCCESS"
            echo "URL : ${env.BUILD_URL}"
        }
        failure {
            echo "[${env.JOB_NAME} #${env.BUILD_NUMBER}] branche ${env.BRANCH_NAME} - FAILURE"
        }
        always {
            cleanWs()
        }
    }
}