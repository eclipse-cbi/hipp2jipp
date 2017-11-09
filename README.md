# HIPP2JIPP

This tool helps to migrate a HIPP (Hudson instance per project) to a JIPP (Jenkins instance per project) in the context of the build infrastructure hosted at the Eclipse Foundation. It can also be used to convert any standalone Hudson server to Jenkins.

Since [Hudson](http://www.hudson-ci.org) and [Jenkins](http://jenkins.io) diverged, their config file format has changed. Therefore a XSLT transformation is used to convert Hudson config files to be compatible with Jenkins.

It currently only transforms job config files (/JENKINS_HOME/jobs/&lt;job name&gt;/config.xml) and build files (/JENKINS_HOME/jobs/&lt;job name&gt;/builds/&lt;number/date&gt;/build.xml). Basic transformation of main config files has been tested, but deemed not necessary for the conversion in the HIPP/JIPP context.

Backups of each transformed file will be created (e.g. config.bak).

## Usage

1. Copy the Jobs directory from the Hudson Home dir to the Jenkins Home dir.

2. Run the HIPP2JIPP tool on the Jenkins Home dir:<br>```java -cp hipp2jipp-<VERSION>.jar org.eclipse.cbi.hipp2jipp.HudsonConfigConverter <JENKINS_HOME> ```

### Converting Views

If the Hudson server has views configured, the command line option ```-cv``` can be used to copy the views to the Jenkins main config:
```java -cp hipp2jipp-<version>.jar org.eclipse.cbi.hipp2jipp.HudsonConfigConverter <JENKINS_HOME> -cv <PATH_TO_HUDSON_MAIN_CONFIG.XML>```

### Converting Nodes/Agents

If the Hudson server has nodes/agents configured, the NodeConverter can be used to create the correct file structure for Jenkins:
```java -cp hipp2jipp-<version>.jar org.eclipse.cbi.hipp2jipp.NodeConverter <PATH_TO_HUDSON_MAIN_CONFIG.XML> <JENKINS_HOME>```

## Known issues
* Git SCM
  * Checking out more than one Git repo to different local directories is (currently) not supported by the Git plugin (workaround: check out one Git repo with the Git plugin and the second with a shell build step)
* Maven 3
  * Maven 3 settings files are not available in Jenkins (specific deploy settings have to be put in separate files manually and referenced in the Maven Build step configuration)
  * "Record fingerprints of Maven 3 artifacts" option is not available in Jenkins
* Shell build step
  * "Temporarily disable this builder" option is not supported out-of-the-box
* Cascading jobs are not supported in Jenkins
  * they should be converted to normal freestyle jobs first
* Priority Sorter Plugin
  * "In [version] 3.x the option 'Allow priorities directly on Jobs' has been removed in favor of the Priority Strategy 'Take the priority from Property on the Job'." (https://wiki.jenkins.io/display/JENKINS/Priority+Sorter+Plugin)

# Credits
This tool was inspired by code from Marcel Schutte that he posted in the jenkins-ci Google group here:
https://groups.google.com/d/msg/jenkinsci-users/arKSTc3Fz3Q/2JsQk9E0_eAJ
