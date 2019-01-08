<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
    <!--<xsl:output method="html" />-->
    <xsl:template match="/">
        <h2>Peer Review History</h2>
        <table class="table table-bordered review-history">
            <tbody>
                <tr>
                    <th>Original Submission
                        <div class="date">
                            <span class="decision-date">September 29, 2018</span>
                        </div>
                    </th>
                </tr>


    <xsl:for-each select="tpr/response">
        <tr>
            <td>

                <div class="decision-letter"
                     itemtype="http://schema.org/Review">
                    <div itemprop="itemReviewed"
                         itemtype="http://schema.org/ScholarlyArticle">

                        <!--<@siteLink handlerName="article" absoluteLink=true queryParameters={"id" : article.doi}-->
                        <!--; articleUrl>-->
                        <!--&lt;!&ndash;<meta itemprop="url" content="${articleUrl}"/>&ndash;&gt;-->
                        <!--</@siteLink>-->

                    </div>

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
                    <span itemprop="author"
                          itemtype="http://schema.org/Person">
                        <span itemprop="name">
                            Lauren Bianchini, Editor
                        </span>
                    </span>
                    <div class="date">
                        <!--<time class="decision-date"-->
                        <!--itemprop="dateCreated"-->
                        <!--datetime="2018-10-01">-->
                        October 1, 2018
                        <!--</time>-->
                    </div>

                    <!-- accordion container -->
                    <div itemprop="reviewBody" class="collapse" id="decisionLetter">

                        <!-- decision letter text -->
                        <!--<p>[decisionLetter.contents]</p>-->
                        <xsl:value-of select="front-stub/title-group/article-title"/>
                        <!--&lt;!&ndash; reviewer attachments &ndash;&gt;-->
                        <!--<dl class="review-files">-->
                        <!--<dt>Attachments</dt>-->
                        <!--<dd>-->
                        <!--<a itemprop="url" href="#" target="_blank" title="Download .docx file">Attachment-->
                        <!--1-->
                        <!--</a>-->
                        <!--(532KB)-->
                        <!--<br>Submitted filename:-->
                        <!--<i>This is my file name.docx</i>-->
                        <!--</dd>-->
                        <!--<dd>-->
                        <!--<a itemprop="url" href="#" target="_blank" title="Download .docx file">Attachment-->
                        <!--2-->
                        <!--</a>-->
                        <!--(946 KB)<br>Submitted filename:-->
                        <!--<i>Shall I compare thee to a summers day.docx</i>-->
                        <!--</dd>-->
                        <!--<dd>-->
                        <!--<a itemprop="url" href="#" target="_blank" title="Download .docx file">Attachment-->
                        <!--3-->
                        <!--</a>-->
                        <!--(772 KB)<br>Submitted filename:-->
                        <!--<i>So much depends upon a red wheelbarrow.docx</i>-->
                        <!--</dd>-->
                        <!--<dd>-->
                        <!--<a itemprop="url" href="#" target="_blank" title="Download .csv file">Attachment 4-->
                        <!--</a>-->
                        <!--(1.1 MB)<br>Submitted filename:-->
                        <!--<i>One Data File 2018-09-27T11_53_36-0700.csv</i>-->
                        <!--</dd>-->
                        <!--</dl>-->
                    </div>
                    <!-- end accordion container -->

                </div>
            </td>
        </tr>
    </xsl:for-each>
    <tr>
        <th>Accepted</th>
    </tr>

</tbody>
        </table>
<div class="tpr-info">
<h3>Open letter on the publication of peer review reports</h3>
<p>PLOS recognizes the benefits of transparency in the peer review process. Therefore, we enable the publication of all
    of the content of peer review and author responses alongside final, published articles. Reviewers remain anonymous,
    unless they choose to reveal their names.
</p>

<p>We encourage other journals to join us in this initiative. We hope that our action inspires the community, including
    researchers, research funders, and research institutions, to recognize the benefits of published peer review reports
    for all parts of the research system.
</p>
<p>Learn more at <a href="http://asapbio.org/letter" target="_blank" title="Link opens in new window">ASAPbio</a>.
</p>
</div>


</xsl:template>
</xsl:stylesheet>
