<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:output method="html" />
  <xsl:template match="/">
    <h2>Peer Review History</h2>
    <table class="table table-bordered review-history">
      <tbody>
        <xsl:for-each select="peer-review/revision">
          <tr>
            <th>
              <xsl:if test="position() = 1">
                Original Submission
              </xsl:if>
              <xsl:if test="position() > 1">
                Revision <xsl:value-of select="position() - 1"/>
              </xsl:if>

              <div class="date">
                <span class="decision-date">January 1, 1979</span>
              </div>
            </th>
          </tr>
          <xsl:if test="sub-article/@article-type = 'author-comment'">
            <tr>
              <td>
                <div class="date">
                  <div class="decision-date">
                    January 1, 1979
                  </div>
                </div>
                <div class="author-response">
                  <p>
                     <!--<xsl:value-of select="sub-article[@article-type = 'author-comment']/front-stub/article-id"/>-->
                    <xsl:copy-of select="sub-article[@article-type = 'author-comment']/body"/>
                  </p>
                </div>
              </td>
            </tr>
          </xsl:if>
          <xsl:if test="sub-article/@specific-use = 'decision-letter'">
            <tr>
              <td>
                <div class="decision-letter">
                  <!-- trigger for expand and collapse -->
                  <a data-toggle="collapse"
                     href="#decisionLetter"
                     role="button"
                     aria-expanded="false"
                     aria-controls="decisionLetter">
                    Decision Letter
                  </a>
                  <!-- end trigger for expand and collapse -->
                  -
                  <span class="author">
                    <span class="name">
                      Lauren Bianchini, Editor
                    </span>
                  </span>
                  <div class="date">
                    <div class="decision-date">
                      January 1, 1979
                    </div>
                  </div>
                  <div>
                    <p>
                      <xsl:copy-of select="sub-article[@specific-use = 'decision-letter']/body"/>
                    </p>
                  </div>
                </div>

              </td>
            </tr>
          </xsl:if>
        </xsl:for-each>

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
</xsl:stylesheet>
