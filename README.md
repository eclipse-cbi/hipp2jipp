# HIPP2JIPP

This tool helps to migrate a HIPP (Hudson instance per project) to a JIPP (Jenkins instance per project) in the context of the build infrastructure hosted at the Eclipse Foundation. It can also be used to convert any standalone Hudson server to Jenkins.

Since [Hudson](http://www.hudson-ci.org) and [Jenkins](http://jenkins.io) diverged, their config file format has changed. Therefore a XSLT transformation is used to convert Hudson config files to be compatible with Jenkins.

It currently only transforms job config files (/JENKINS_HOME/jobs/<job name>/config.xml) and build files (/JENKINS_HOME/jobs/<job name>/builds/<number/date>/build.xml). Basic transformation of main config files has been tested, but deemed not necessary for the conversion in the HIPP/JIPP context.

Backups of each transformed file will be created (e.g. config.bak).

## Usage

1. Copy the Jobs directory from the Hudson Home dir to the Jenkins Home dir.

2. Run the HIPP2JIPP tool on the Jenkins Home dir:<br>```java -cp hipp2jipp-0.0.1-SNAPSHOT.jar org.eclipse.cbi.hipp2jipp.XslTransformer <JENKINS_HOME> ```

## Known issues
* Git SCM
  * Checking out more than one Git repo to different local directories is (currently) not supported by the Git plugin (workaround: check out one Git repo with the Git plugin and the second with a shell build step)
* Maven 3
  * Maven 3 settings files are not available in Jenkins (specific deploy settings have to be put in separate files manually and referenced in the Maven Build step configuration)
  * "Record fingerprints of Maven 3 artifacts" option is not available in Jenkins
* Shell build step
  * "Temporarily disable this builder" option is not supported out-of-the-box
* Cascading jobs are not supported in Jenkins
* Migration of the following features is not supported yet:
  * Slave configs (can be copied from main config.xml to separate config.xml files in JENKINS_HOME/nodes/<node name>/)
  * Views (can be copied from main config.xml)
* Priority Sorter Plugin
  * "In [version] 3.x the option 'Allow priorities directly on Jobs' has been removed in favor of the Priority Strategy 'Take the priority from Property on the Job'." (https://wiki.jenkins.io/display/JENKINS/Priority+Sorter+Plugin)

# Credits
This tool was inspired by code from Marcel Schutte that he posted in the jenkins-ci Google group here:
https://groups.google.com/d/msg/jenkinsci-users/arKSTc3Fz3Q/2JsQk9E0_eAJ
