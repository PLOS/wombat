<!--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


  <xsl:output doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
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
    <span class="overline" style="text-decoration: overline">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="price">
    <span class="price">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="roman">
    <span class="roman">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="sans-serif">
    <span class="sans-serif">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="sc">
    <span class="small-caps" style="font-variant: small-caps">
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
    <span class="underline" style="text-decoration: underline">
      <xsl:apply-templates/>
    </span>
  </xsl:template>


</xsl:stylesheet>
