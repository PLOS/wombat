<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign tabPage = "authors" />


<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

<#include "articleHeader.ftl" />

  <section class="article-body content">

    <#include "tabs.ftl" />
    <@displayTabList 'Authors' />
    <h1>About the Authors</h1>

    <#list authorListAffiliationMap?keys as affiliation>
      <p>
        <span class="author-list">${authorListAffiliationMap[affiliation]}</span>
        <br/>
        ${affiliation}
      </p>
    </#list>

    <#if correspondingAuthors?? && correspondingAuthors?size gt 0>
      <#if correspondingAuthors?size == 1>
        <h2>Corresponding Author</h2>
        <p class="about-author">${correspondingAuthors[0]}</p>
      <#else>
        <h2>Corresponding Authors</h2>
        <ul>
          <#list correspondingAuthors as correspondingAuthor>
            <li class="about-author">${correspondingAuthor}</li>
          </#list>
        </ul>
      </#if>
    </#if>

    <#if competingInterests?size gt 0>
      <h2>Competing Interests</h2>
      <#list competingInterests as competingInterest>
        <p>${competingInterest}</p>
      </#list>
    </#if>

    <#if authorContributions?size gt 0>
      <h2>Author Contributions</h2>
      <#list authorContributions as contribution>
        <p>${contribution}</p>
      </#list>
    </#if>

  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>

<#include "../common/footer/footer.ftl" />
<#include "articleJs.ftl" />
<@renderJs />

<#include "aside/crossmarkIframe.ftl" />
<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
<div class="reveal-modal-bg"></div>
<div id="article-lightbox" class="reveal-modal" data-reveal>

</div>
</body>
</html>


