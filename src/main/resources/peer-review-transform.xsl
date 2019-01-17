<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
>
  <xsl:output method="html"/>

  <xsl:template match="/">
    <h2>Peer Review History</h2>
    <table class="table table-bordered review-history">
      <tbody>
        <xsl:apply-templates/>
        <tr>
          <th>Formally Accepted</th>
        </tr>
      </tbody>
    </table>
    <div class="tpr-info">
      <h3>Open letter on the publication of peer review reports</h3>
      <p>PLOS recognizes the benefits of transparency in the peer review process. Therefore, we enable the publication
        of
        all
        of the content of peer review and author responses alongside final, published articles. Reviewers remain
        anonymous,
        unless they choose to reveal their names.
      </p>

      <p>We encourage other journals to join us in this initiative. We hope that our action inspires the community,
        including
        researchers, research funders, and research institutions, to recognize the benefits of published peer review
        reports
        for all parts of the research system.
      </p>
      <p>Learn more at <a href="http://asapbio.org/letter" target="_blank" title="Link opens in new window">ASAPbio</a>.
      </p>
    </div>
  </xsl:template>

  <xsl:template match="revision">
    <tr>
      <th>
        <xsl:choose>
          <xsl:when test="position() = 1">
            Original Submission
          </xsl:when>
          <xsl:otherwise>
            Revision <xsl:value-of select="position() - 1"/>
          </xsl:otherwise>
        </xsl:choose>
        <div class="date">
          <span class="decision-date">
            <xsl:value-of select=".//named-content[@content-type = 'letter-date']"/>
          </span>
        </div>
      </th>
    </tr>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="body">
    <xsl:apply-templates/>
    <xsl:if test="supplementary-material">
      <dl class="review-files">
        <dt>Attachments</dt>
        <xsl:apply-templates select="supplementary-material"/>
      </dl>
    </xsl:if>
  </xsl:template>

  <xsl:template match="p">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="list-item">
    <li>
      <xsl:copy-of select="node()"/>
    </li>
  </xsl:template>

  <xsl:template match="ext-link">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@xlink:href"/>
      </xsl:attribute>
      <xsl:value-of select="text()"/>>
    </a>
  </xsl:template>

  <xsl:template match="supplementary-material">
    <dd class="supplementary-material">
      <a class="sm-label" href="#">
        <xsl:value-of select="label"/>
      </a>
      <div class="sm-caption">
        <xsl:copy-of select="caption/p/text()"/>
        <i>
          <xsl:value-of select="caption/p/named-content"/>
        </i>
      </div>
    </dd>
  </xsl:template>

  <xsl:template match="sub-article[@article-type = 'author-comment']">
    <tr>

      <td>
        <!-- author response -->
        <a data-toggle="collapse"
           href="#decisionLetter2"
           role="button"
           aria-expanded="false"
           aria-controls="decisionLetter2">
          [Author Response expand/collapse]
        </a>
        <div class="date">
          <time class="response-date">
            ---
          </time>
        </div>

        <!-- accordion container -->
        <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
          <div class="author-response">
            <p>
              bogus author response text for:
              <xsl:value-of select="front-stub/article-id"/>
            </p>
          </div>
        </div>
        <!-- end accordion container -->
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="sub-article[@specific-use = 'decision-letter']">
    <tr>
      <td>
        <div class="decision-letter" itemscope=""
             itemtype="http://schema.org/Review">
          <div itemprop="itemReviewed" itemscope=""
               itemtype="http://schema.org/ScholarlyArticle">
            <meta itemprop="url" content="articleUrl" />
          </div>

          <!-- trigger for expand and collapse -->
          <a data-toggle="collapse"
             href="#decisionLetter3"
             role="button"
             aria-expanded="false"
             aria-controls="decisionLetter3">
            [Decision Letter expand/collapse]
          </a>
          <!-- end trigger for expand and collapse -->
          -
          <span itemprop="author" itemscope=""
                itemtype="http://schema.org/Person">
            <span itemprop="name">
              Nico Donkelope
            </span>
          </span>
          , Editor
          <div class="date">
            <time class="decision-date"
                  itemprop="dateCreated"
                  datetime="">
              <xsl:value-of select=".//named-content[@content-type = 'letter-date']"/>
            </time>
          </div>

          <!-- accordion container -->
          <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
            <div class="decision-letter-body">
              <p>
                <xsl:apply-templates select="body"/>
              </p>
            </div>
          </div>
          <!-- end accordion container -->
        </div>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match = "named-content"/>
</xsl:stylesheet>
