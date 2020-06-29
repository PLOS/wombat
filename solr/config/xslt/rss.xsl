<?xml version='1.0' encoding='UTF-8'?>

<!-- 
  Simple transform of Solr query results to RSS
 -->

<xsl:stylesheet version='1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

  <xsl:output
       method="xml"
       encoding="utf-8"
       media-type="application/xml"
  />
  <xsl:template match='/'>
    <rss version="2.0">
       <channel>
         <title>PLOS Solr RSS 2.0 Feed</title>
         <link>http://api.plos.org/search</link>
         <description>
           PLOS Search RSS XSL Transform
         </description>
         <language>en-us</language>
         <docs>http://api.plos.org/</docs>
         <xsl:apply-templates select="response/result/doc"/>
       </channel>
    </rss>
  </xsl:template>
  
  <!-- search results xslt -->
  <xsl:template match="doc">
    <item>
      <title><xsl:value-of select="str[@name='title_display']"/></title>
      <link>
        http://dx.plos.org/<xsl:value-of select="str[@name='id']"/>
      </link>
      <description>
        <xsl:value-of select="arr[@name='abstract']"/>
      </description>
      <pubDate><xsl:value-of select="date[@name='publication_date']"/></pubDate>
    </item>
  </xsl:template>
</xsl:stylesheet>
