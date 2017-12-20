import groovy.json.JsonOutput
pipeline {
    agent any
    tools {
        maven '3.5.2'    // 3.5.2 relates to the label applied to a given version of Maven
    }
    stages {
        stage('Build') {
            steps {
                populateGlobalVariables()
                //slackSend (color: '#FFFF00', message: "STARTED: '${env.BRANCH_NAME} #${env.BUILD_NUMBER}' (${env.BUILD_URL})")
                notifySlack("Starting", 'fuse-java-builds', [
                    [
                        title: "${env.JOB_NAME}, build #${env.BUILD_NUMBER}",
                        title_link: "${env.BUILD_URL}",
                        color: "warning",
                        text: "${author},
                        "mrkdwn_in": ["fields"],
                        fields: [
                            [
                                title: "Branch",
                                value: "${env.GIT_BRANCH}",
                                short: true
                            ],
                            [
                                title: "Last Commit",
                                value: "${message}",
                                short: false
                            ]
                        ]
                    ]
                ])
                sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent -Dmaven.test.failure.ignore=true compile'
            }
        }
        stage('Unit tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('SonarCloud FUSE') {
                    sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent compile test-compile test sonar:sonar'
                }
            }
        }
        stage('Quality gate') {
            steps {
                // Just in case something goes wrong, pipeline will be killed after a timeout
                timeout(time: 1, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }
        stage('Checkstyle') {
            steps {
                sh 'mvn checkstyle:check'
            }
            // using the following results in an error in the pipeline - ERROR: None of the test reports contained any result
            //post {
            //    always {
            //        junit 'target/checkstyle-result.xml'
            //    }
            //}
            // this will simply show a blank report if the checkstyle check is successful
            post {
                failure {
                    publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: 'target',
                        reportFiles: 'checkstyle-result.xml',
                        reportName: 'Checkstyle report'
                    ])
                }
            }
        }
        stage('Deploy') {
            when {
                not {
                    // for some reason this does not work. a branch name of feature/issue-84-pipeline-updates does not match
                    // expression { BRANCH_NAME ==~ /^feature\/|hotfix\/|bugfix\// }
                    anyOf {
                        expression { BRANCH_NAME.startsWith('feature/') }
                        expression { BRANCH_NAME.startsWith('hotfix/') }
                        expression { BRANCH_NAME.startsWith('bugfix/') }
                    }
                }
            }
            steps {
                slackSend (color: '#FFFF00', message: "DEPLOYING: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
                // for the moment just re-do all the maven phases, I tried doing just jar:jar, but it wasn't working with cloud foundry
                sh 'mvn package'

                pushToCloudFoundry(
                    target: 'https://api.run.pivotal.io/',
                    organization: 'FUSE',
                    cloudSpace: 'development',
                    credentialsId: 'cf-credentials',
                    manifestChoice: [manifestFile: 'manifest-dev.yml']
                    // pluginTimeout: 240 // default value is 120
                )
            }
        }
        stage('Integration tests') {
            when {
                expression { BRANCH_NAME ==~ /^PR-\d+$/ }
            }
            steps {
                sh 'mvn verify'
            }
            post {
               always {
                 junit 'target/failsafe-reports/*.xml'
               }
            }
        }
        stage('Performance tests') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Running performance tests...'
            }
        }
        stage('Shutdown') {
            when {
                not {
                    anyOf {
                        expression { BRANCH_NAME.startsWith('feature/') }
                        expression { BRANCH_NAME.startsWith('hotfix/') }
                        expression { BRANCH_NAME.startsWith('bugfix/') }
                    }
                }
            }
            steps {
                echo 'Shutting down app'
                timeout(time: 2, unit: 'MINUTES') {
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'cf-credentials', usernameVariable: 'CF_USERNAME', passwordVariable: 'CF_PASSWORD']]) {
                        // make sure the password does not contain single quotes otherwise the escaping fails
                        sh "cf login -u ${CF_USERNAME} -p '${CF_PASSWORD}' -o FUSE -s development -a https://api.run.pivotal.io"
                        sh 'cf stop fuse-rest-dev'
                        sh 'cf logout'
                    }
                }
            }
        }
    }
    post {
        success {
            slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
        failure {
            slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }   
    }
}

 def notifySlack(text, channel, attachments) {
    def slackURL = 'https://hooks.slack.com/services/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'
    def jenkinsIcon = 'https://wiki.jenkins-ci.org/download/attachments/2916393/logo.png'

    def payload = JsonOutput.toJson([text: text,
        channel: channel,
        username: "Jenkins",
        icon_url: jenkinsIcon,
        attachments: attachments
    ])

    sh "curl -X POST --data-urlencode \'payload=${payload}\' ${slackURL}"
}
def author = ""
def getGitAuthor = {
    def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
    author = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${commit}").trim()
}

def message = ""
def getLastCommitMessage = {
    message = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
}

def populateGlobalVariables = {
    getLastCommitMessage()
    getGitAuthor()
}
