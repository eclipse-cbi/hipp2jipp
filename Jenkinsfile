pipeline {
  agent any
  options {
      buildDiscarder(logRotator(numToKeepStr:'10'))
  }

  tools {
    jdk 'oracle-jdk8-latest'
  }

  stages {
    stage('Checkout') {
      steps {
        cleanWs()
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