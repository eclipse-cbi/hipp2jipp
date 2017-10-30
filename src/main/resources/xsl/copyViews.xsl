<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:param name="sourceFile"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="views">
    <xsl:for-each select="document($sourceFile)/hudson/views">
      <xsl:copy-of select="." />
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>