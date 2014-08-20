<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />
 <#--
 if articleType is any of:
 correction, retraction, expression of concern
 else if
 authors == 0
 hide authors tab
 -->

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">

  <input type="hidden" id="rawPubDate" value="${article.date}" />

  <#include "../common/header/header.ftl" />
  <div class="plos-row">
    <section class="article-body">
      <div class="classifications">
        <p class="license-short" id="licenseShort"><span class="icon"></span>Open Access</p>

        <#if article.articleType=="Research Article">
        <p class="peer-reviewed" id="peerReviewed"><span class="icon"></span>Peer-reviewed</p>
        </#if>

        <div class="article-type" id="artType">${article.articleType!""}</div>
      </div>

        <h1 id="artTitle"> ${article.title} </h1>

        <ul class="date-doi">
          <li id="artPubDate">Published:  </li>
          <li id="artDoi">DOI: ${article.doi} </li>
        </ul>

     <#-- <#include "tabs.ftl" />-->

      <ul class="article-tabs" data-tab>
        <li class="tab-title active">

          <a href="#tabArticle1" class="article-tab-1" id="tabArticle">Article</a>
        <#-- http://one-dpro.plosjournals.org/wombat/DesktopPlosNtds/article?id=10.1371/journal.pntd.0000104 -->
        </li>

      <#-- check if there are authors -->
      <#if article.authors?? && article.authors?size gt 0>
      <#-- check if article type is correction, retraction, concern -->
        <#if ["correction", "retraction", "expression-of-concern"]?seq_contains(article.articleType)>
        <#--suppress author tab -->
        <#else>
        <#-- ok to show the author tab -->
          <li class="tab-title">
            <a href="#tabArticle2" class="article-tab-2" id="tabAuthors">About the Authors</a>
          <#---->
          </li>
        </#if>
      </#if>
        <li class="tab-title"><a href="#tabArticle3" class="article-tab-3" id="tabMetrics">Metrics</a></li>
        <li class="tab-title"><a href="#tabArticle4" class="article-tab-4" id="tabComments">Comments</a></li>
        <li class="tab-title"><a href="#tabArticle5" class="article-tab-5" id="tabRelated">Related Content</a></li>

      </ul>

      <div class="article-text" id="artText">
          ${articleText}
      </div>


    </section>
    <aside class="article-column">

    </aside>
  </div>
  <#include "../common/footer/footer.ftl" />

  <@renderJs />
  <script src="<@siteLink path="resource/js/components/dateparse.js"/>"></script>
  <script src="<@siteLink path="resource/js/pages/article.js"/>"></script>

  </body>
</html>
