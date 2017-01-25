<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:template match="/project">
    <!--
    <xsl:message terminate="no">
      WARNING: Unmatched element:
      <xsl:value-of select="name()" />
    </xsl:message>
    -->
    <project>
      <xsl:apply-templates select="actions | description"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'logRotator']"/>
      <xsl:apply-templates select="keepDependencies"/>
      <properties>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'DiskUsageProperty')]"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'parametersDefinitionProperties']"/>
      </properties>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'scm']"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'quietPeriod']"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'scmCheckoutRetryCount']"/>
      <xsl:apply-templates select="canRoam | disabled"/>
      <xsl:apply-templates select="blockBuildWhenDownstreamBuilding | blockBuildWhenUpstreamBuilding"/>
      <!-- <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'blockBuildWhen')]"/> -->
      <xsl:apply-templates select="project-properties/entry [string/text() = 'jdk']"/>
      <triggers>
        <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'hudson-triggers-')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'GerritTrigger')]"/>
      </triggers>
      <xsl:apply-templates select="concurrentBuild"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'customWorkspace']"/>
      <builders>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'builders']" />
      </builders>
      <publishers>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'HtmlPublisher')]" />
        <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'hudson-tasks-')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'JacocoPublisher')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'GitPublisher')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'SonarPublisher')]" />
      </publishers>
      <buildWrappers>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'Xvnc')]" />
      </buildWrappers>
    </project>
  </xsl:template>

  <xsl:template match="actions | description | keepDependencies | disabled | canRoam | blockBuildWhenDownstreamBuilding | blockBuildWhenUpstreamBuilding | concurrentBuild">
    <xsl:copy-of select="." />
  </xsl:template>

  <!-- Standard identity template to copy input document -->
  <!-- 
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>
  -->
  <!-- filter out unwanted tags -->
  <!-- TODO: instead of filtering out, only define what should be copied -->
  <!-- 
  <xsl:template match="creationTime | cascadingChildrenNames | cascading-job-properties | blockBuildWhenDownstreamBuilding | blockBuildWhenUpstreamBuilding | properties | cleanWorkspaceRequired | scm | concurrentBuild" />
  -->

  <xsl:template match="/project/project-properties/entry">
    <xsl:choose>
      <xsl:when test="string = 'parametersDefinitionProperties'">
      <!-- <xsl:when test="string = 'parametersDefinitionProperties' or string = 'builders'"> -->
        <!-- TODO: hier weitermachen -> maven-builder umsetzen auf hudson.tasks.Maven -->
        <xsl:copy-of select="*/originalValue/*" />
      </xsl:when>
      <xsl:when test="starts-with(string/text(), 'hudson-tasks-') or starts-with(string/text(), 'hudson-triggers-') or contains(string/text(), 'Publisher') or contains(string/text(), 'Xvnc') or contains(string/text(), 'GerritTrigger')">
        <xsl:variable name="tagName">
          <xsl:value-of select="*/originalValue/@class" />
        </xsl:variable>
        <xsl:element name="{$tagName}">
          <xsl:copy-of select="*/originalValue/*" />
        </xsl:element>
      </xsl:when>
      <xsl:when test="contains(string/text(), 'DiskUsageProperty')">
        <xsl:element name="hudson.plugins.disk__usage.DiskUsageProperty">
          <xsl:copy-of select="*/originalValue/*" />
        </xsl:element>
      </xsl:when>
      <!-- TODO: can this be improved? -->
      <xsl:when test="*/originalValue [@class = 'hudson.plugins.git.GitSCM']">
          <xsl:apply-templates select="*/originalValue [@class = 'hudson.plugins.git.GitSCM']"/>
      </xsl:when>
      <xsl:when test="*/originalValue [@class = 'string' or @class = 'int' or @class = 'boolean']">
        <xsl:variable name="tagName">
          <xsl:copy-of select="string/text()" />
        </xsl:variable>
        <xsl:element name="{$tagName}">
          <xsl:copy-of select="*/originalValue/text()" />
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="tagName">
          <xsl:copy-of select="string/text()" />
        </xsl:variable>
        <xsl:element name="{$tagName}">
          <xsl:attribute name="class">
            <xsl:value-of select="*/originalValue/@class" />
          </xsl:attribute>
          <xsl:copy-of select="*/originalValue/*" />
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/project/project-properties/entry [string = 'builders']">
    <xsl:choose>
      <xsl:when test="describable-list-property/originalValue/maven-builder">
        <xsl:element name="hudson.tasks.Maven">
          <targets>
            <xsl:value-of select="describable-list-property/originalValue/maven-builder/config/goals" />
          </targets>
          <mavenName>
            <xsl:value-of select="describable-list-property/originalValue/maven-builder/config/installationId" />
          </mavenName>
          <pom>
            <xsl:value-of select="describable-list-property/originalValue/maven-builder/config/pomFile" />
          </pom>
          <usePrivateRepository>
            <xsl:value-of select="describable-list-property/originalValue/maven-builder/config/privateRepository" />
          </usePrivateRepository>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="*/originalValue/*" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*/originalValue [@class = 'hudson.plugins.git.GitSCM']">
    <xsl:element name="scm">
      <xsl:attribute name="class">
        <xsl:value-of select="@class" />
      </xsl:attribute>
      <userRemoteConfigs>
        <xsl:for-each select="remoteRepositories/RemoteConfig">
          <hudson.plugins.git.UserRemoteConfig>
            <name><xsl:value-of select="name/text()" /></name>
            <refspec>
              <xsl:text>+</xsl:text>
              <xsl:value-of select="fetch/org.eclipse.jgit.transport.RefSpec/srcName/text()" />
              <xsl:text>:</xsl:text>
              <xsl:value-of select="fetch/org.eclipse.jgit.transport.RefSpec/dstName/text()" />
            </refspec>
            <url>
              <!-- TODO: concatenate strings? -->
              <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/scheme/text()" />
              <xsl:text>://</xsl:text>
              <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/host/text()" />
              <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/path/text()" />
            </url>
          </hudson.plugins.git.UserRemoteConfig>
        </xsl:for-each>
      </userRemoteConfigs>
      <xsl:copy-of select="branches" />
      <xsl:copy-of select="doGenerateSubmoduleConfigurations" />
      <xsl:copy-of select="submoduleCfg" />
      <xsl:if test="remoteRepositories/RemoteConfig/relativeTargetDir [text() != '']">
        <extensions>
          <hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
            <xsl:copy-of select="remoteRepositories/RemoteConfig/relativeTargetDir" />
          </hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
        </extensions>
      </xsl:if>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>