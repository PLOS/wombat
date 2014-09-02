<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">

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
      <#if crossPub?size gt 0>
        <li>
          Featured in
          <#list crossPub as crossPubJournal>
            <#if crossPubJournal.href??>
              <a href="${crossPubJournal.href}">${crossPubJournal.title}</a>
            <#else>
            ${crossPubJournal.title}
            </#if>
          <#-- TODO: Format in case crossPub has more than one -->
          </#list>
        </li>
      </#if>
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
