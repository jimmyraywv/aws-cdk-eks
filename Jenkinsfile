// def awsCredentials = [[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-personal']]

pipeline {
  agent {
    dockerfile true
  }

  options {
    disableConcurrentBuilds()
    parallelsAlwaysFailFast()
    timestamps()
    // withCredentials(awsCredentials)
  }

  stages {
    // stage('Deploy') {
    //   steps {
    //     sh 'cdk deploy --require-approval=never'
    //   }
    // }

    // stage('Destroy') {
    //   steps {
    //     sh 'cdk destroy --require-approval=never'
    //   }
    // }

    stage('Synth') {
      steps {
        sh 'cdk synth'
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}
