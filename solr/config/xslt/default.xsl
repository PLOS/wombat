<?xml version='1.0' encoding='UTF-8'?>
<!--
  Simple transform of Solr query results to HTML
 -->
<xsl:stylesheet version='1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
>

  <xsl:output media-type="text/html" encoding="UTF-8"/> 
  
  <xsl:variable name="title" select="concat('Solr search results (',response/result/@numFound,' documents)')"/>
  
  <xsl:template match='/'>
    <html>
      <head>
        <title><xsl:value-of select="$title"/></title>
        <xsl:call-template name="css"/>
      </head>
      <body>
        <h1><xsl:value-of select="$title"/></h1>
        <div class="note">
          PLOS Search API Results.  Check out <a href="http://api.plos.org/solr/faq/">http://api.plos.org/solr/faq/</a> for
          details.
        </div>
        <xsl:apply-templates select="response/result/doc"/>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="doc">
    <xsl:variable name="pos" select="position()"/>
    <div class="doc">
      <table width="100%">
        <xsl:apply-templates>
          <xsl:with-param name="pos"><xsl:value-of select="$pos"/></xsl:with-param>
        </xsl:apply-templates>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="doc/*[@name='id']" priority="100">
    <xsl:param name="pos"></xsl:param>
    <tr>
      <th class="name">
        <xsl:value-of select="@name"/>
      </th>
      <td class="value">
        <a>
          <xsl:attribute name="href">http://dx.plos.org/<xsl:value-of select="."/></xsl:attribute>
          <xsl:value-of select="."/>
        </a>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="doc/arr" priority="100">
    <tr>
      <th class="name">
        <xsl:value-of select="@name"/>
      </th>
      <td class="value">
        <ul>
        <xsl:for-each select="*">
          <li><xsl:value-of select="."/></li>
        </xsl:for-each>
        </ul>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="doc/*">
    <tr>
      <th class="name">
        <xsl:value-of select="@name"/>
      </th>
      <td class="value">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="*"/>
  
  <xsl:template name="css">
    <style type="text/css">
      body { font-family: "Lucida Grande", sans-serif }
      th { font-style: italic; font-weight: normal; width: 300px; text-align: left; vertical-align:top;}
      td { vertical-align: top; }
      ul { margin: 0; padding: 0; list-style:none; }
      ul li { margin 0; padding 0; }
      .note { font-size:80%; }
      div.doc { margin-top: 1em; border-top: solid grey 1px; }
      div.doc table { margin: 15px 0 0 0; }
      .exp { display: none; font-family: monospace; white-space: pre; }
    </style>
  </xsl:template>

</xsl:stylesheet>
