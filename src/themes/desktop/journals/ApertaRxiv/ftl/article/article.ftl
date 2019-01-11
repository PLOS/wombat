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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title, articleDoi = article.doi />
<#assign tabPage="Article"/>
<#assign adPage="Article"/>
<#assign cssFile="ApertaRxiv-article.css"/>
<#assign isArticlePage = true />

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/counterJs.ftl" />
<#include "../common/orcidConfigJs.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<article>
  <div class="article-header-block">
    <div class="article-meta">
      <span class="preprint-checks" id="preprintChecks">Passed Preprint Checks</span>
      <span class="peer-reviewed" id="peerReviewed">Not Peer Reviewed</span>
    </div>
    
    <div class="article-info">
      <div class="date-doi">
        <div class="date" id="artPubDate"><@formatJsonDate date="${article.publicationDate}" format="MMMM d, yyyy" /></div>
        <div class="doi" id="artDoi">
          <#include "../macro/doiAsLink.ftl" />
          <@doiAsLink article.doi />
        </div>
      </div>
      <#if article.isPreprintOfDoi?has_content>
        <div class="published">
          <a href="https://dx.doi.org/${article.isPreprintOfDoi}">&gt;&gt; See the published version</a>
        </div>
      </#if>
    </div>

    <#--todo: design-->
    <#if orcidAuthenticationError?has_content>
      <div>
        ERROR: ${orcidAuthenticationError}
      </div>
    </#if>
    <a id="connect-orcid-link" onclick="openORCID()"><img id="orcid-id-logo" src="https://orcid.org/sites/default/files/images/orcid_16x16.png" width='16' height='16' alt="ORCID logo"/>Create or Connect your ORCID iD</a>

    <h1 class="article-title" id="artTitle"><@xform xml=article.title/></h1>

    <div class="article-buttons">
      <a class="fulltext"
         href="<@siteLink handlerName="assetFile" queryParameters=(articlePtr + {"type": "printable"}) />"
         id="downloadPdf" target="_blank">
        <div class="icon"></div><div class="text">Full Text</div>
      </a>
      <div class="share" data-component="dropdown" data-target="#share-options">
        <div class="icon"></div><div class="text">Share</div>
      </div>
      <a class="xml"
         href="<@siteLink handlerName="assetFile" queryParameters=(articlePtr + {"type": "manuscript"}) />"
         id="downloadXml">
        <div class="icon"></div><div class="text">XML</div>
      </a>
    </div>

  </div>

  <div class="article-content-block">
    <#include "authorItem.ftl" />
    <#include "authorList.ftl" />

    <div class="metrics hide clearfix" id="metrics">
      <div class="alm-info">
        <div class="counters">
          <div><h1 id="counter-views"></h1><div>page views</div></div>
          <div><h1 id="counter-pdf"></h1><div>PDF downloads</div></div>
          <div><h1 id="counter-xml"></h1><div>XML downloads</div></div>
        </div>
        <div class="counter-error" id="counter-error"></div>
      </div>
    </div>

    <section class="article-body">
      <div id="articleText"
           data-component="collapse" data-toggle="false"
           data-toggle-class="section-toggle" data-box-class="section-box">
        ${articleText}

        <div class="toc-section subjects" id="subjects">
          <h2>Related Subjects</h2><a title="Related Subjects"></a>
          <div>
            <#if categoryTerms??>
              <#list categoryTerms as categoryTerm>
                <div class="subject">
                  <@themeConfig map="journal" value="journalKey" ; journalKey>
                    <@siteLink handlerName="simpleSearch"
                    queryParameters= {
                    "filterSubjects": categoryTerm,
                    "filterJournals": journalKey,
                    "q": ""}; href>
                      <a class="taxo-term" title="Search for articles about ${categoryTerm}"
                         href="${href}">${categoryTerm}</a>
                    </@siteLink>
                  </@themeConfig>
                </div>
              </#list>
            <#else>
              <div class="subject empty">No Content</div>
            </#if>
          </div>
        </div>
        <div class="toc-section comments" id="comments">
          <h2>Comments</h2><a title="Comments"></a>
          <div id="commentstree"></div>
        </div>
      </div>
    </section>
  </div>

  <div class="share-options hide" id="share-options">
    <ul>
      <#include 'aside/shareOptions.ftl' />
      <#include 'aside/shareInserts.ftl' />
    </ul>
  </div>
</article>

<#include "../common/footer/footer.ftl" />

<@js target="resource/js/aperta_article.js"></@js>
<@js target="resource/js/aperta_counter.js"></@js>
<@js target="resource/js/pages/comments.js" />

</body>
</html>
