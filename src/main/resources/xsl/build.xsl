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
       <xsl:element name="entry">
         <xsl:copy-of select="*" />
       </xsl:element>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>