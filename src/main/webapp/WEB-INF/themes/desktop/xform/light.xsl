<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


  <xsl:output doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
              doctype-system="http://www.w3.org/TR/html4/loose.dtd"
              encoding="UTF-8"/>

  <xsl:key name="element-by-id" match="*[@id]" use="@id"/>

  <!-- Enabling retrieval of cross-references to objects -->
  <xsl:key name="xref-by-rid" match="xref" use="@rid"/>
  <xsl:template match="/">

    <xsl:apply-templates />

  </xsl:template>
  <xsl:template match="title-group/article-title" mode="metadata">
    <h1 class="document-title">
      <xsl:apply-templates />
      <xsl:if test="../subtitle">:</xsl:if>
    </h1>
  </xsl:template>

  <xsl:template match="bold">
    <strong>
      <xsl:apply-templates/>
    </strong>
  </xsl:template>


  <xsl:template match="italic">
    <i>
      <xsl:apply-templates/>
    </i>
  </xsl:template>


  <xsl:template match="monospace">
    <tt>
      <xsl:apply-templates/>
    </tt>
  </xsl:template>


  <xsl:template match="overline">
    <span class="txt-overline" style="text-decoration: overline">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="price">
    <span class="price">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="roman">
    <span class="txt-roman">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="sans-serif">
    <span class="txt-sans-serif" style="font-family: sans-serif; font-size: 80%">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="sc">
    <span class="txt-small-caps" style="font-variant: small-caps">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="strike">
    <span style="text-decoration: line-through">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="sub">
    <sub>
      <xsl:apply-templates/>
    </sub>
  </xsl:template>


  <xsl:template match="sup">
    <sup>
      <xsl:apply-templates/>
    </sup>
  </xsl:template>


  <xsl:template match="underline">
    <span style="text-decoration: underline">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


</xsl:stylesheet>
