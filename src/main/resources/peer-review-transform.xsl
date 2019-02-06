<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:plos="http://plos.org"
>
  <xsl:function name="plos:get-file-extension">
    <xsl:param name="input" as="xs:string"/>
    <xsl:sequence 
        select="if (contains($input,'.'))
                then concat('.', tokenize($input,'\.')[last()])
                else $input"/>
  </xsl:function>

  <xsl:output method="html"/>

  <xsl:template match="/">
    <h2 class="page-title">Peer Review History</h2>
    <table class="review-history">
      <tbody>
        <xsl:apply-templates />
      </tbody>
    </table>
    <div class="peer-review-open-letter">
      <h3 class="section-title">Open letter on the publication of peer review reports</h3>
      <p>PLOS recognizes the benefits of transparency in the peer review process. Therefore, we enable the publication of all of the content of peer review and author responses alongside final, published articles. Reviewers remain anonymous, unless they choose to reveal their names.</p>
      <p>We encourage other journals to join us in this initiative. We hope that our action inspires the community, including researchers, research funders, and research institutions, to recognize the benefits of published peer review reports for all parts of the research system.</p>
      <p>
        Learn more at
        <a href="http://asapbio.org/letter" target="_blank" title="Link opens in new window">ASAPbio</a>
        .
      </p>
    </div>
  </xsl:template>
  
  <xsl:template match="revision">
    <tr>
      <th class="revision">
        <span class="letter__date">
          <xsl:value-of select=".//named-content[@content-type = 'letter-date']" />
        </span>
        <xsl:choose>
          <xsl:when test="position() = 1">
            <span class="letter__title">Original Submission</span>
          </xsl:when>
          <xsl:otherwise>
            <span class="letter__title">
              Revision
              <xsl:value-of select="position() - 1" />
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </th>
    </tr>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="sub-article[@specific-use = 'decision-letter']">
    <tr>
      <td class="letter">
        <div class="decision-letter" itemscope="" itemtype="http://schema.org/Review">
          <div itemprop="itemReviewed" itemscope="" itemtype="http://schema.org/ScholarlyArticle">
            <meta itemprop="url" content="articleUrl" />
            <time class="letter__date" itemprop="dateCreated" datetime="">
              <xsl:value-of select=".//named-content[@content-type = 'letter-date']" />
            </time>
            <div class="letter__title">
              <!-- trigger for expand and collapse -->
              <a class="letter--toggle" data-toggle="collapse" href="#decisionLetter3">[Decision Letter]</a>
              <!-- end trigger for expand and collapse -->
              -
              <span itemprop="author" itemscope="" itemtype="http://schema.org/Person">
                <span itemprop="name">Nico Donkelope</span>
              </span>
              , Editor
            </div>
            <!-- accordion container -->
            <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
              <div class="letter__body">
                  <xsl:apply-templates select="body" />
              </div>
            </div>
            <!-- end accordion container -->
          </div>
        </div>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="sub-article[@specific-use = 'acceptance-letter']">
    <tr>
      <th class="revision">
        <span class="letter__title">Formally Accepted</span>
      </th>
    </tr>
    <tr>
      <td class="letter">
        <div class="acceptance-letter" itemscope="" itemtype="http://schema.org/Review">
          <div itemprop="itemReviewed" itemscope="" itemtype="http://schema.org/ScholarlyArticle">
            <meta itemprop="url" content="articleUrl" />
            <time class="letter__date" itemprop="dateCreated" datetime="">
              <xsl:value-of select=".//named-content[@content-type = 'letter-date']" />
            </time>
            <div class="letter__title">
              <!-- trigger for expand and collapse -->
              <a class="letter--toggle" data-toggle="collapse" href="#">[Acceptance Letter]</a>
              <!-- end trigger for expand and collapse -->
            </div>
            <!-- accordion container -->
            <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
              <div class="letter__body">
                  <xsl:apply-templates select="body" />
              </div>
            </div>
            <!-- end accordion container -->
          </div>
        </div>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="sub-article[@article-type = 'author-comment']">
    <tr>
      <td class="letter">
        <div class="author-response">
          <!-- author response -->
          <time class="letter__date">
            <xsl:value-of select=".//named-content[@content-type = 'author-response-date']" />
          </time>
          <div class="letter__title">
            <a class="letter--toggle" data-toggle="collapse" href="#decisionLetter2">[Author Response]</a>
          </div>
          <!-- accordion container -->
          <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
            <div class="letter__body">
                <xsl:apply-templates select="body" />
            </div>
          </div>
          <!-- end accordion container -->
        </div>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template match="body">
    <xsl:apply-templates select="*[not(self::supplementary-material)]" />
    <xsl:if test="supplementary-material">
      <dl class="review-files">
        <dt class="review-files__title">Attachments</dt>
        <xsl:apply-templates select="supplementary-material" />
      </dl>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="p">
    <xsl:copy>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="supplementary-material">
      <dd class="supplementary-material">
        <a class="supplementary-material__label coloration-white-on-color" 
           href="{concat('file?id=10.1371/journal.',@id,'&amp;type=supplementary')}" 
           title="{string-join(('Download',plos:get-file-extension(normalize-space(caption/p/named-content)),'file'),' ')}"
           target="_blank">
          <xsl:value-of select="label"/>
        </a>
      <div class="supplementary-material__caption">
        <xsl:copy-of select="caption/p/text()"/>
        <i>
          <xsl:value-of select="caption/p/named-content" />
        </i>
      </div>
    </dd>
  </xsl:template>
  
  <xsl:template match="list">
    <ul>
      <xsl:apply-templates />
    </ul>
  </xsl:template>
  
  <xsl:template match="list-item">
    <li>
      <xsl:copy-of select="node()" />
    </li>
  </xsl:template>
  
  <xsl:template match="ext-link">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@xlink:href" />
      </xsl:attribute>
      <xsl:value-of select="text()" />
      &gt;
    </a>
  </xsl:template>
  
  <xsl:template match="named-content" />
</xsl:stylesheet>
