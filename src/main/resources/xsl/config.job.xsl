<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:template match="/project | /maven2-moduleset | /matrix-project">
    <!--
    <xsl:message terminate="no">
      WARNING: Unmatched element:
      <xsl:value-of select="name()" />
    </xsl:message>
    -->
    <xsl:variable name="rootName">
      <xsl:copy-of select="name()" />
    </xsl:variable>
    <xsl:element name="{$rootName}">
      <xsl:apply-templates select="actions | description"/>
      <xsl:apply-templates select="keepDependencies"/>
      <properties>
        <xsl:apply-templates select="properties/hudson.security.AuthorizationMatrixProperty"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'logRotator']"/>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'DiskUsageProperty')]"/>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'promoted_builds')]"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'parametersDefinitionProperties']"/>
      </properties>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'scm']"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'quietPeriod']"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'scmCheckoutRetryCount']"/>
      <xsl:apply-templates select="canRoam | disabled"/>
      <xsl:apply-templates select="blockBuildWhenDownstreamBuilding | blockBuildWhenUpstreamBuilding"/>
      <!-- <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'blockBuildWhen')]"/> -->
      <xsl:apply-templates select="project-properties/entry [string/text() = 'jdk']"/>
      <xsl:apply-templates select="authToken"/>
      <triggers>
        <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'hudson-triggers-')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'GerritTrigger')]"/>
      </triggers>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'concurrentBuild']"/>
      <xsl:if test="/matrix-project">
        <axes>
          <xsl:apply-templates select="project-properties/entry [string/text() = 'axes']"/>
        </axes>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'combinationFilter']"/>
      </xsl:if>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'customWorkspace']"/>
      <xsl:if test="/maven2-moduleset">
        <!-- Matches for Maven job -->
        <xsl:apply-templates select="rootModule"/>
        <xsl:apply-templates select="goals"/>
        <xsl:element name="mavenName">apache-maven-latest</xsl:element>
        <xsl:apply-templates select="mavenOpts"/>
        <xsl:apply-templates select="aggregatorStyleBuild"/>
        <xsl:apply-templates select="incrementalBuild"/>
        <xsl:apply-templates select="usePrivateRepository"/>
        <xsl:apply-templates select="ignoreUpstremChanges"/>
        <xsl:apply-templates select="archivingDisabled"/>
        <xsl:apply-templates select="resolveDependencies"/>
        <xsl:apply-templates select="processPlugins"/>
        <xsl:apply-templates select="mavenValidationLevel"/>
        <xsl:apply-templates select="reporters"/>
      </xsl:if>
      <builders>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'builders']" />
      </builders>
      <xsl:choose>
        <xsl:when test="/maven2-moduleset">
          <xsl:apply-templates select="publishers"/>
        </xsl:when>
        <xsl:otherwise>
          <publishers>
            <!-- fix duplicate AggregatedTestResultPublisher entry -->
            <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'Publisher') and not(contains(string/text(), 'AggregatedTestResultPublisher'))]" />
            <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'hudson-tasks-')]" />
            <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'hudson-plugins-parameterizedtrigger')]" />
            <xsl:apply-templates select="project-properties/entry [starts-with(string/text(), 'hudson-plugins-ws_cleanup')]" />
          </publishers>
        </xsl:otherwise>
       </xsl:choose>
      <buildWrappers>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'BuildTimeoutWrapper')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'Xvnc')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'cleanWorkspaceRequired')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'TimestamperBuildWrapper')]" />
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'LockWrapper')]"/>
      </buildWrappers>
      <xsl:if test="/matrix-project">
      <executionStrategy class="hudson.matrix.DefaultMatrixExecutionStrategyImpl">
        <xsl:apply-templates select="runSequentially"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'touchStoneCombinationFilter']"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'touchStoneResultCondition']"/>
      </executionStrategy>
      </xsl:if>
    </xsl:element>
  </xsl:template>

  <!-- Promotion config  -->
  <xsl:template match="/hudson.plugins.promoted__builds.PromotionProcess">
    <xsl:variable name="rootName">
      <xsl:copy-of select="name()" />
    </xsl:variable>
    <xsl:element name="{$rootName}">
      <xsl:apply-templates select="keepDependencies"/>
      <properties>
        <xsl:apply-templates select="properties/hudson.security.AuthorizationMatrixProperty"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'logRotator']"/>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'DiskUsageProperty')]"/>
        <xsl:apply-templates select="project-properties/entry [contains(string/text(), 'promoted_builds')]"/>
        <xsl:apply-templates select="project-properties/entry [string/text() = 'parametersDefinitionProperties']"/>
      </properties>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'scm']"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'quietPeriod']"/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'scmCheckoutRetryCount']"/>
      <xsl:apply-templates select="scm"/>
      <xsl:apply-templates select="canRoam"/>
      <xsl:apply-templates select="disabled"/>
      <xsl:apply-templates select="blockBuildWhenDownstreamBuilding"/>
      <xsl:apply-templates select="blockBuildWhenUpstreamBuilding"/>
      <triggers/>
      <xsl:apply-templates select="project-properties/entry [string/text() = 'concurrentBuild']"/>
      <xsl:apply-templates select="conditions"/>
      <xsl:apply-templates select="icon"/>
      <xsl:apply-templates select="isVisible"/>
      <xsl:apply-templates select="buildSteps"/>
    </xsl:element>
  </xsl:template>

  <!-- Matches for Promotions config -->
  <xsl:template match="scm | conditions | icon | isVisible | buildSteps">
    <xsl:copy-of select="." />
  </xsl:template>

  <xsl:template match="actions | description | keepDependencies | disabled | canRoam | blockBuildWhenDownstreamBuilding | blockBuildWhenUpstreamBuilding |
                       hudson.security.AuthorizationMatrixProperty | authToken ">
    <xsl:copy-of select="." />
  </xsl:template>

  <!-- Matches for Maven job -->
  <xsl:template match="rootModule | goals | mavenOpts | aggregatorStyleBuild | incrementalBuild | usePrivateRepository | ignoreUpstremChanges | archivingDisabled |
                       resolveDependencies | processPlugins | mavenValidationLevel | reporters | publishers">
    <xsl:copy-of select="." />
  </xsl:template>

  <!-- Matches for Matrix job -->
  <xsl:template match="runSequentially">
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

  <xsl:template match="/*/project-properties/entry">
    <!-- TODO: can this be improved? -->
    <xsl:choose>
      <xsl:when test="*/originalValue [@class = 'hudson.plugins.git.GitSCM']">
        <xsl:apply-templates select="*/originalValue [@class = 'hudson.plugins.git.GitSCM']" />
      </xsl:when>
      <xsl:when test="*/originalValue [@class = 'hudson.scm.SubversionSCM']">
        <xsl:apply-templates select="*/originalValue [@class = 'hudson.scm.SubversionSCM']" />
      </xsl:when>
      <xsl:when test="*/originalValue [@class = 'hudson.plugins.cloneworkspace.CloneWorkspaceSCM']">
        <xsl:element name="scm">
          <xsl:attribute name="class">
            <xsl:value-of select="*/originalValue/@class" />
          </xsl:attribute>
            <xsl:copy-of select="*/originalValue/*" />
        </xsl:element>
      </xsl:when>
      <xsl:when test="string = 'builders'">
        <xsl:apply-templates select="describable-list-property/originalValue"/>
      </xsl:when>
      <xsl:when test="starts-with(string/text(), 'hudson-tasks-') or
                      starts-with(string/text(), 'hudson-triggers-') or
                      contains(string/text(), 'Publisher') or
                      contains(string/text(), 'Xvnc') or
                      contains(string/text(), 'GerritTrigger') or
                      contains(string/text(), 'TimestamperBuildWrapper') or
                      contains(string/text(), 'parameterizedtrigger')">
        <xsl:variable name="tagName">
          <xsl:value-of select="*/originalValue/@class" />
        </xsl:variable>
        <xsl:if test="not($tagName = '')">
          <xsl:element name="{$tagName}">
            <xsl:choose>
              <!-- special case for BuildTrigger -->
              <xsl:when test="$tagName = 'hudson.tasks.BuildTrigger'">
                <xsl:copy-of select="*/originalValue/childProjects" />
                <xsl:element name="threshold">
                  <xsl:copy-of select="*/originalValue/threshold/name" />
                  <xsl:copy-of select="*/originalValue/threshold/ordinal" />
                  <xsl:element name="color">
                    <xsl:if test="*/originalValue/threshold [color = 'GREEN']">
                      <xsl:text>BLUE</xsl:text>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
              </xsl:when>
              <!-- special case for parameterizedtrigger-BuildTrigger -->
              <xsl:when test="$tagName = 'hudson.plugins.parameterizedtrigger.BuildTrigger'">
                <xsl:element name="configs">
                  <xsl:for-each select="*/originalValue/configs/hudson.plugins.parameterizedtrigger.BuildTriggerConfig">
                    <xsl:element name="hudson.plugins.parameterizedtrigger.BuildTriggerConfig">
                      <xsl:apply-templates select="configs [@class='java.util.Collections$EmptyList']" />
                      <xsl:copy-of select="* [not(@class='java.util.Collections$EmptyList')]" />
                    </xsl:element>
                  </xsl:for-each>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="*/originalValue/*" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- 
        <xsl:variable name="tagName">
          <xsl:copy-of select="string/text()" />
        </xsl:variable>
        <xsl:element name="{$tagName}">
          <xsl:attribute name="class">
            <xsl:value-of select="*/originalValue/@class" />
          </xsl:attribute>
          <xsl:copy-of select="*/originalValue/*" />
        </xsl:element>
        -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Fix class -->
  <xsl:template match="configs [@class='java.util.Collections$EmptyList']">
    <xsl:element name="configs">
      <xsl:attribute name="class">empty-list</xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- Filter out tags that exist in the XML, but are not set, by just doing nothing-->
  <xsl:template match="/*/project-properties/entry [not(*/originalValue) and (*/propertyOverridden [text() = 'false'])]" />

  <!-- ParameterDefinitionProperties -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'parametersDefinitionProperties']">
    <xsl:copy-of select="*/originalValue/*" />
  </xsl:template>

  <!-- Matrix project axes -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'axes']">
    <xsl:copy-of select="*/originalValue/*" />
  </xsl:template>

  <!-- Combination filter -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'combinationFilter']">
    <xsl:element name="combinationFilter">
      <xsl:copy-of select="*/originalValue/text()" />
    </xsl:element>
  </xsl:template>

  <!-- Touchstone combination filter -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'touchStoneCombinationFilter']">
    <xsl:element name="touchStoneCombinationFilter">
      <xsl:copy-of select="*/originalValue/text()" />
    </xsl:element>
  </xsl:template>

  <!-- Touchstone result condition -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'touchStoneResultCondition']">
    <xsl:element name="touchStoneResultCondition">
      <xsl:copy-of select="*/originalValue/*" />
    </xsl:element>
  </xsl:template>

  <!-- String, Int, Boolean, etc -->
  <xsl:template match="/*/project-properties/entry [(*/originalValue/@class = 'string' or */originalValue/@class = 'int' or */originalValue/@class = 'boolean' or */originalValue/@class = '') and not(string/text() = 'cleanWorkspaceRequired')]">
    <xsl:variable name="tagName">
      <xsl:copy-of select="string/text()" />
    </xsl:variable>
    <xsl:element name="{$tagName}">
      <xsl:copy-of select="*/originalValue/text()" />
    </xsl:element>
  </xsl:template>

  <!-- LogRotator -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'logRotator' and log-rotator-property/originalValue/@class = 'hudson.tasks.LogRotator']">
    <xsl:element name="jenkins.model.BuildDiscarderProperty">
      <xsl:element name="strategy">
        <xsl:attribute name="class">
          <xsl:value-of select="*/originalValue/@class" />
        </xsl:attribute>
        <xsl:copy-of select="*/originalValue/*" />
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- cleanWorkspaceRequired -->
  <xsl:template match="/project/project-properties/entry [string/text() = 'cleanWorkspaceRequired' and boolean-property/originalValue/@class = 'boolean' and boolean-property/originalValue/text() = 'true']">
    <xsl:element name="hudson.plugins.ws__cleanup.PreBuildCleanup">
      <xsl:element name="deleteDirs">false</xsl:element>
      <xsl:element name="cleanupParameter"/>
      <xsl:element name="externalDelete"/>
    </xsl:element>
  </xsl:template>

  <!-- DiskUsage -->
  <xsl:template match="/*/project-properties/entry [contains(string/text(), 'DiskUsageProperty')]">
    <xsl:element name="hudson.plugins.disk__usage.DiskUsageProperty">
      <xsl:copy-of select="*/originalValue/*" />
    </xsl:element>
  </xsl:template>

  <!-- LockWrapper -->
  <xsl:template match="/*/project-properties/entry [contains(string/text(), 'LockWrapper')]">
    <xsl:element name="hudson.plugins.locksandlatches.LockWrapper">
      <xsl:copy-of select="*/originalValue/*" />
    </xsl:element>
  </xsl:template>

  <!-- PromotedBuilds -->
  <xsl:template match="/*/project-properties/entry [contains(string/text(), 'promoted_builds')]">
    <xsl:element name="hudson.plugins.promoted__builds.JobPropertyImpl">
      <xsl:copy-of select="*/originalValue/*" />
    </xsl:element>
  </xsl:template>

  <!-- JDK -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'jdk']">
    <xsl:element name="jdk">
      <xsl:if test="contains(*/originalValue/text(), '1.8')">
        <xsl:text>jdk1.8.0-latest</xsl:text>
      </xsl:if>
      <xsl:if test="contains(*/originalValue/text(), '1.7')">
        <xsl:text>jdk1.7.0-latest</xsl:text>
      </xsl:if>
      <xsl:if test="contains(*/originalValue/text(), '1.6')">
        <xsl:text>jdk1.6.0-latest</xsl:text>
      </xsl:if>
      <xsl:if test="contains(*/originalValue/text(), '1.5')">
        <xsl:text>jdk1.5.0-latest</xsl:text>
      </xsl:if>
    </xsl:element>
  </xsl:template>

  <!-- "Delete workspace when build is done" -->
  <xsl:template match="/*/project-properties/entry [starts-with(string/text(), 'hudson-plugins-ws_cleanup')]">
    <xsl:element name="hudson.plugins.ws__cleanup.WsCleanup">
      <xsl:copy-of select="*/originalValue/*" />
    </xsl:element>
  </xsl:template>

  <!-- TODO: Externalize? -->
  <!-- BuildTimeOut -->
  <xsl:template match="/*/project-properties/entry [contains(string/text(), 'BuildTimeoutWrapper') and external-property/originalValue/@class = 'hudson.plugins.build_timeout.BuildTimeoutWrapper']">
    <xsl:element name="hudson.plugins.build__timeout.BuildTimeoutWrapper">
      <xsl:element name="strategy">
        <xsl:choose>
          <xsl:when test="*/originalValue/timeoutType/text() = 'absolute'">
            <xsl:attribute name="class">
              <xsl:text>hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy</xsl:text>
            </xsl:attribute>
            <!-- TODO: simplify copy-of ? -->
            <xsl:element name="timeoutMinutes">
              <xsl:copy-of select="*/originalValue/timeoutMinutes/text()" />
            </xsl:element>
          </xsl:when>
          <xsl:when test="*/originalValue/timeoutType/text() = 'elastic'">
            <xsl:attribute name="class">
              <xsl:text>hudson.plugins.build_timeout.impl.ElasticTimeOutStrategy</xsl:text>
            </xsl:attribute>
            <!-- TODO: simplify copy-of ? -->
            <xsl:element name="timeoutPercentage">
              <xsl:copy-of select="*/originalValue/timeoutPercentage/text()" />
            </xsl:element>
            <!--
            Missing in Build Timeout Plugin version 1.11: 
            <numberOfBuilds>3</numberOfBuilds>
            <failSafeTimeoutDuration>false</failSafeTimeoutDuration>
            -->
            <xsl:element name="timeoutMinutesElasticDefault">
              <xsl:copy-of select="*/originalValue/timeoutMinutesElasticDefault/text()" />
            </xsl:element>
          </xsl:when>
          <xsl:when test="*/originalValue/timeoutType/text() = 'likelyStuck'">
            <xsl:attribute name="class">
              <xsl:text>hudson.plugins.build_timeout.impl.LikelyStuckTimeOutStrategy</xsl:text>
            </xsl:attribute>
            <!--
            Missing in Build Timeout Plugin version 1.11:
            <timeoutEnvVar>boing</timeoutEnvVar>
            -->
          </xsl:when>
          <xsl:otherwise>
            <!-- Works with Build Timeout Plugin version 1.7 (same as absolute case above) -->
            <xsl:attribute name="class">
              <xsl:text>hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy</xsl:text>
            </xsl:attribute>
            <!-- TODO: simplify copy-of ? -->
            <xsl:element name="timeoutMinutes">
              <xsl:copy-of select="*/originalValue/timeoutMinutes/text()" />
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:element>
      <xsl:element name="operationList">
        <xsl:if test="*/originalValue/failBuild/text() = 'true'">
          <xsl:element name="hudson.plugins.build__timeout.operations.FailOperation" />
        </xsl:if>
        <xsl:if test="*/originalValue/writingDescription/text() = 'true'">
          <xsl:element name="hudson.plugins.build__timeout.operations.WriteDescriptionOperation" />
          <!--
          Missing in Build Timeout Plugin version 1.11:
          <description>Bla</description>
          -->
        </xsl:if>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- builders catch-all -->
  <xsl:template match="/*/project-properties/entry [string/text() = 'builders']/describable-list-property/originalValue">
    <xsl:for-each select="./*">
      <xsl:choose>
        <xsl:when test="name() = 'maven-builder' or name() = 'hudson.tasks.Shell' or name() = 'hudson.plugins.groovy.SystemGroovy'">
          <xsl:apply-templates select="." />
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="." />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- maven-builder -->
  <xsl:template match="maven-builder">
    <xsl:element name="hudson.tasks.Maven">
      <xsl:element name="targets">
        <xsl:value-of select="config/goals" />
        <xsl:for-each select="config/profiles/string">
          <xsl:text> -P</xsl:text>
          <xsl:value-of select="text()" />
        </xsl:for-each>
      </xsl:element>
      <xsl:element name="mavenName">
        <!-- apache-maven-latest is the savest bet -->
        <xsl:text>apache-maven-latest</xsl:text>
        <!-- ><xsl:value-of select="config/installationId" /> -->
      </xsl:element>
      <xsl:element name="jvmOptions">
        <xsl:value-of select="config/mavenOpts" />
      </xsl:element>
      <xsl:element name="pom">
        <xsl:value-of select="config/pomFile" />
      </xsl:element>
      <xsl:element name="properties">
        <xsl:for-each select="config/properties/entries/entry">
          <xsl:value-of select="@name" />
          <xsl:text>=</xsl:text>
          <xsl:value-of select="@value" />
          <xsl:text>&#10;</xsl:text>
        </xsl:for-each>
      </xsl:element>
      <xsl:element name="usePrivateRepository">
        <xsl:value-of select="config/privateRepository" />
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- hudson.tasks.Shell -->
  <xsl:template match="hudson.tasks.Shell">
    <xsl:element name="hudson.tasks.Shell">
      <xsl:copy-of select="command" />
    </xsl:element>
  </xsl:template>

  <!-- hudson.plugins.groovy.SystemGroovy -->
  <xsl:template match="hudson.plugins.groovy.SystemGroovy">
    <xsl:element name="hudson.plugins.groovy.SystemGroovy">
      <xsl:element name="source">
        <xsl:attribute name="class">
          <xsl:value-of select="scriptSource/@class"/>
        </xsl:attribute>
        <xsl:copy-of select="scriptSource/*" />
      </xsl:element>
      <xsl:copy-of select="bindings" />
      <xsl:copy-of select="classpath" />
    </xsl:element>
  </xsl:template>

  <!-- hudson.plugins.groovy.Groovy -->
  <!-- no extra template necessary, <xsl:otherwise> block in line 283 takes care of it -->

  <!-- Git -->
  <xsl:template match="*/originalValue [@class = 'hudson.plugins.git.GitSCM']">
    <xsl:element name="scm">
      <xsl:attribute name="class">
        <xsl:value-of select="@class" />
      </xsl:attribute>
      <xsl:element name="userRemoteConfigs">
        <xsl:for-each select="remoteRepositories/RemoteConfig">
          <xsl:element name="hudson.plugins.git.UserRemoteConfig">
            <name><xsl:value-of select="name/text()" /></name>
            <refspec>
              <xsl:choose>
                <!-- TODO: make this more universal -->
                <xsl:when test="fetch/org.eclipse.jgit.transport.RefSpec/srcName [text() = '$GERRIT_REFSPEC']">
                  <xsl:value-of select="fetch/org.eclipse.jgit.transport.RefSpec/srcName/text()" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>+</xsl:text>
                  <xsl:value-of select="fetch/org.eclipse.jgit.transport.RefSpec/srcName/text()" />
                  <xsl:text>:</xsl:text>
                  <xsl:value-of select="fetch/org.eclipse.jgit.transport.RefSpec/dstName/text()" />
                </xsl:otherwise>
              </xsl:choose>
            </refspec>
            <url>
              <xsl:if test="uris/org.eclipse.jgit.transport.URIish/scheme">
                <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/scheme/text()" />
                <xsl:text>://</xsl:text>
              </xsl:if>
              <xsl:if test="uris/org.eclipse.jgit.transport.URIish/user [text() != '']">
                <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/user/text()" />
                <xsl:text>@</xsl:text>
              </xsl:if>
              <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/host/text()" />
              <xsl:if test="uris/org.eclipse.jgit.transport.URIish/path [substring(text(),1,1) != '/']">
                  <xsl:text>:</xsl:text>
              </xsl:if>
              <xsl:value-of select="uris/org.eclipse.jgit.transport.URIish/path/text()" />
            </url>
          </xsl:element>
        </xsl:for-each>
      </xsl:element>
      <xsl:copy-of select="branches" />
      <xsl:copy-of select="doGenerateSubmoduleConfigurations" />
      <xsl:copy-of select="submoduleCfg" />
      <extensions>
        <xsl:if test="remoteRepositories/RemoteConfig/relativeTargetDir [text() != '']">
          <hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
            <xsl:copy-of select="remoteRepositories/RemoteConfig/relativeTargetDir" />
          </hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
        </xsl:if>
        <xsl:if test="clean [text() = 'true']">
          <hudson.plugins.git.extensions.impl.CleanCheckout/>
        </xsl:if>
        <xsl:if test="wipeOutWorkspace [text() = 'true']">
          <hudson.plugins.git.extensions.impl.WipeWorkspace/>
        </xsl:if>
        <xsl:if test="recursiveSubmodules [text() = 'true']">
          <hudson.plugins.git.extensions.impl.SubmoduleOption>
            <disableSubmodules>false</disableSubmodules>
            <recursiveSubmodules>true</recursiveSubmodules>
            <trackingSubmodules>false</trackingSubmodules>
            <reference></reference>
            <parentCredentials>false</parentCredentials>
          </hudson.plugins.git.extensions.impl.SubmoduleOption>
        </xsl:if>
        <xsl:if test="pruneBrances [text() = 'true']">
          <hudson.plugins.git.extensions.impl.PruneStaleBranch/>
        </xsl:if>
        <xsl:if test="buildChooser [@class = 'com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTriggerBuildChooser']">
          <hudson.plugins.git.extensions.impl.BuildChooserSetting>
            <buildChooser class="com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTriggerBuildChooser">
              <xsl:copy-of select="buildChooser/separator" />
            </buildChooser>
          </hudson.plugins.git.extensions.impl.BuildChooserSetting>
        </xsl:if>
      </extensions>
    </xsl:element>
  </xsl:template>

  <!-- SVN -->
  <xsl:template match="*/originalValue [@class = 'hudson.scm.SubversionSCM']">
    <xsl:element name="scm">
      <xsl:attribute name="class">
        <xsl:value-of select="@class" />
      </xsl:attribute>
      <xsl:copy-of select="* [not(name()='workspaceUpdater')]" />
      <xsl:element name="workspaceUpdater">
        <xsl:attribute name="class">
          <!-- TODO: fix for other values -->
          <xsl:choose>
            <xsl:when test="workspaceUpdater [@class = 'hudson.scm.subversion.CheckoutWithLocationFoldersCleanupUpdater']">
              <xsl:text>hudson.scm.subversion.CheckoutUpdater</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="workspaceUpdater/text()" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>