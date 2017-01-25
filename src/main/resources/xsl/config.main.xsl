<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:template match="/hudson">
    <!--
    <xsl:message terminate="no">
      WARNING: Unmatched element:
      <xsl:value-of select="name()" />
    </xsl:message>
    -->
    <hudson>
      <xsl:apply-templates select="version | numExecutors | mode | systemMessage"/>
      <xsl:apply-templates select="jdks"/>
      <xsl:apply-templates select="viewsTabBar | myViewsTabBar"/>
      <xsl:apply-templates select="slaves"/>
      <xsl:apply-templates select="quietPeriod"/>
      <xsl:apply-templates select="scmCheckoutRetryCount"/>
      <xsl:apply-templates select="views"/>
      <xsl:apply-templates select="label"/>
      <xsl:apply-templates select="nodeProperties"/>
      <xsl:apply-templates select="globalNodeProperties"/>
    </hudson>
  </xsl:template>

  <xsl:template match="version | numExecutors | mode | systemMessage | jdks | viewsTabBar | myViewsTabBar | slaves | quietPeriod | scmCheckoutRetryCount | views | label | nodeProperties | globalNodeProperties">
    <xsl:copy-of select="." />
  </xsl:template>

</xsl:stylesheet>