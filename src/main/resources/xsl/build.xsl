<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- Standard identity template to copy input document -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*/hudson.plugins.git.util.Build/revision">
    <xsl:element name="marked">
      <xsl:copy-of select="*" />
    </xsl:element>
    <revision reference="../marked"/>
  </xsl:template>

  <xsl:template match="*/hudson.model.CauseAction/causes">
    <xsl:element name="causeBag">
      <xsl:attribute name="class">linked-hash-map</xsl:attribute>
      <xsl:for-each select="*">
        <xsl:element name="entry">
          <xsl:copy-of select="." />
          <int><xsl:value-of select="position()" /></int>
        </xsl:element>
      </xsl:for-each>

    </xsl:element>
  </xsl:template>

  <xsl:template match="*/hudson.tasks.junit.TestResultAction/descriptions [@class='java.util.concurrent.ConcurrentHashMap']">
    <xsl:element name="descriptions">
      <xsl:attribute name="class">concurrent-hash-map</xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- Filter out the tags by just doing nothing-->
  <xsl:template match="*/hudson.plugins.disk__usage.BuildDiskUsageAction" />
  <xsl:template match="*/maven-build-record" />
  <xsl:template match="*/maven-build-action" />

</xsl:stylesheet>