<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">
  <div id="titleFloater" class="topAttached">
    <div class="plos-row">
      <div class="title-authors">
    <h1 id="artTitle"> ${article.title} </h1>
  <#include "authorList.ftl" />
        </div>
      <div class="logo-close">plos x</div>
    </div>
  </div>
  <input type="hidden" id="rawPubDate" value="${article.date}" />

  <#include "../common/header/header.ftl" />
  <div class="plos-row">
    <section class="article-body">
      <div class="classifications">
        <p class="license-short" id="licenseShort">Open Access</p>

        <#if article.articleType=="Research Article">
        <p class="peer-reviewed" id="peerReviewed">Peer-reviewed</p>
        </#if>

        <div class="article-type" id="artType">${article.articleType!""}</div>
      </div>

        <h1 id="artTitle"> ${article.title} </h1>
        <#include "authorList.ftl" />


      <ul class="date-doi">
        <li id="artPubDate">Published:  </li>
        <li id="artDoi">DOI: ${article.doi} </li>
      </ul>

      <#include "tabs.ftl" />

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
