pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        git(url: 'https://github.com/eclipse/hipp2jipp', changelog: true)
      }
    }
    stage('Build') {
      steps {
        withMaven(maven: 'apache-maven-latest') {
          sh 'mvn clean verify'
        }
        
      }
    }
    stage('Archive') {
      steps {
        archiveArtifacts 'target/*.jar'
        junit '**/target/surefire-reports/TEST-*.xml'
      }
    }
  }
}