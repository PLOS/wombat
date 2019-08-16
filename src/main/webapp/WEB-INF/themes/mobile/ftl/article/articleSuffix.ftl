<#--
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

<nav>
  <ul class="article-buttons">
  <#macro articleButton handlerName>
    <li class="menuitem">
      <a class="btn-lg" href="<@siteLink handlerName=handlerName queryParameters={"id": article.doi} />">
        <span class="arrow">View</span>
        <#nested/>
      </a>
    </li>
  </#macro>
  <#include "sectionLinkSettings.ftl" />

  <#if figures?has_content>
    <@articleButton "figuresPage">
      Figures (${figures?size})
    </@articleButton>
  </#if>

  <#if commentCount.all &gt; 0 && isSectionLinkDisplayed("Comments")>
    <@articleButton "articleComments">
      Reader Comments (${commentCount.all})
    </@articleButton>
  </#if>

  <#if isSectionLinkDisplayed("Authors")>
    <@articleButton "articleAuthors">
      About the Authors
    </@articleButton>
  </#if>

  <#if isSectionLinkDisplayed("Metrics")>
    <@articleButton "articleMetrics">
      Metrics
    </@articleButton>
  </#if>

  <#if isSectionLinkDisplayed("Related")>
    <@articleButton "articleRelatedContent">
      Media Coverage
    </@articleButton>
  </#if>

  <#if peerReview??>
    <@articleButton "articlePeerReview">
      Peer Review
    </@articleButton>
  </#if>

  </ul>
</nav><#--end article buttons-->

<nav class="article-save-options">

<#macro articleSaveButton address>
  <div class="button-row">
    <a class="rounded coloration-white-on-color full" href="${address}">
      <#nested/>
    </a>
  </div>
</#macro>

<#if articleItems[article.doi].files?keys?seq_contains("printable")>
  <@siteLink handlerName="assetFile" queryParameters=(articlePtr + {"type": "printable"}) ; href>
    <@articleSaveButton href>Download Article (PDF)</@articleSaveButton>
  </@siteLink>
</#if>

<@siteLink handlerName="citationDownloadPage" queryParameters={"id": article.doi} ; href>
  <@articleSaveButton href>Download Citation</@articleSaveButton>
</@siteLink>

<div class="button-row">
  <a class="rounded coloration-white-on-color full" href="mailto:?subject=${titleFix}&body=I%20thought%20you%20would%20find%20this%20article%20interesting.%20From%20${journalTitle}:%20http://dx.plos.org/${article.doi}">Email this Article</a>
</div>

</nav><#--end article-save-options-->
</article>
