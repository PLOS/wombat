<?xml version='1.0' encoding='UTF-8'?>

<xsl:stylesheet version='1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

  <xsl:output
       method="xml"
       encoding="utf-8"
       media-type="application/xml"
  />

  <xsl:template match='/'>
    <xsl:variable name="query" select="response/lst[@name='responseHeader']/lst[@name='params']/str[@name='q']"/>
    <feed xmlns="http://www.w3.org/2005/Atom">
      <title>PLOS Solr Atom 1.0 Feed</title>
      <subtitle>
        PLOS Search ATOM XSL Transform
      </subtitle>
      <author>
        <name>PLOS</name>
        <email>webmaster@plos.org</email>
      </author>
      <link rel="self" type="application/atom+xml" 
            href="http://api.plos.org/search?q={$query}&amp;wt=xslt&amp;tr=atom.xsl"/>
      <updated>
        <xsl:value-of select="response/result/doc[position()=1]/date[@name='timestamp']"/>
      </updated>
      <id>tag:api.plos.org,2013:atom</id>
      <xsl:apply-templates select="response/result/doc"/>
    </feed>
  </xsl:template>
    
  <!-- search results xslt -->
  <xsl:template match="doc">
    <xsl:variable name="id" select="str[@name='id']"/>
    <entry>
      <title><xsl:value-of select="str[@name='title_display']"/></title>
      <link href="http://dx.plos.org/{$id}"/>
      <id>tag:api.plos.org,2013:<xsl:value-of select="$id"/></id>
      <summary><xsl:value-of select="arr[@name='abstract']"/></summary>
      <updated><xsl:value-of select="date[@name='publication_date']"/></updated>
    </entry>
  </xsl:template>

</xsl:stylesheet>
