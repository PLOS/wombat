<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="http://dtd.nlm.nih.gov/xsl/util"
                xmlns:mml="http://www.w3.org/1998/Math/MathML"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:aml="http://topazproject.org/aml/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-result-prefixes="util xsl xlink mml xs aml dc">
<xsl:template name="citation">
    <strong>Citation:&#032;</strong>
        <!-- authors -->
    <xsl:for-each select="contrib-group/contrib[@contrib-type='author'][position() &lt; 8]">
    <xsl:choose>
      <xsl:when test="position() = 7">
        <xsl:text>et al. </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="collab">
            <xsl:apply-templates select="collab"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- 1/4/12: we'll need to adjust this when we add name-style eastern-->
            <xsl:apply-templates select="name/surname"/>
            <xsl:if test="name/given-names">
              <xsl:text> </xsl:text>
            </xsl:if>
            <xsl:call-template name="makeInitials">
              <xsl:with-param name="x">
                <xsl:value-of select="name/given-names"/>
              </xsl:with-param>
            </xsl:call-template>
            <!-- don't include the period following the suffix -->
            <xsl:if test="string-length(name/suffix) > 0">
              <xsl:text> </xsl:text>
              <xsl:choose>
                <xsl:when test="substring(name/suffix,string-length(name/suffix))='.'">
                  <xsl:value-of select="substring(name/suffix,1,string-length(name/suffix)-1)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="name/suffix"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="position() != last()">
          <xsl:text>, </xsl:text>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
        <!-- pub year -->
    <xsl:variable name="collectionYear" select="pub-date[@pub-type='collection']/year"/>
    <xsl:variable name="ppubYear" select="pub-date[@pub-type='ppub']/year"/>
    <xsl:variable name="epubYear" select="pub-date[@pub-type='epub']/year"/>
    <xsl:variable name="epreprintYear" select="pub-date[@pub-type='epreprint']/year"/>
    <xsl:if test="$collectionYear | $ppubYear | $epubYear | $epreprintYear">
    <xsl:text> (</xsl:text>
    <xsl:choose>
      <xsl:when test="$collectionYear | $ppubYear">
        <xsl:value-of select="$collectionYear | $ppubYear"/>
      </xsl:when>
      <xsl:when test="$epubYear">
        <xsl:value-of select="$epubYear"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$epreprintYear"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>) </xsl:text>
    </xsl:if>
        <!-- article title -->
    <xsl:apply-templates select="title-group/article-title" mode="metadata-citation"/>
    <xsl:variable name="at" select="normalize-space(title-group/article-title)"/>
        <!-- add a period unless there's other valid punctuation -->
    <xsl:if
    test="substring($at,string-length($at))!='?' and substring($at,string-length($at))!='!' and substring($at,string-length($at))!='.'">
    <xsl:text>.</xsl:text>
    </xsl:if>
    <xsl:text> </xsl:text>
        <!-- journal/volume/issue/enumber/doi -->
    <xsl:value-of select="../journal-meta/journal-id[@journal-id-type='nlm-ta']"/>
    <xsl:text> </xsl:text>
    <xsl:choose>
    <xsl:when test="not(volume)">:</xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="volume"/>(<xsl:value-of select="issue"/>):
    </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:if test="elocation-id">
    <xsl:value-of select="elocation-id"/>.
    </xsl:if>
        https://doi.org/<xsl:value-of select="article-id[@pub-id-type='doi']"/>
  </xsl:template>
</xsl:stylesheet>