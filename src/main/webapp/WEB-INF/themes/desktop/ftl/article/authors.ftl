<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title, articleDoi = article.doi />
<#assign tabPage = "authors" />


<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

<#include "articleHeader.ftl" />

  <section class="article-body content">

    <#include "tabs.ftl" />
    <@displayTabList 'Authors' />
    <h1>About the Authors</h1>
    <#include "authorItem.ftl" />
      <dl>
    <#list authors as author><#-- Before the expander -->
      <#if author_index lt (maxAuthorsToShow - 1) >
        <@authorItemFull author author_index author_has_next true false false/>
      </#if>
    </#list>
      </dl>
    <#list authorListAffiliationMap?keys as affiliation>
      <p>
        <span class="author-list">${authorListAffiliationMap[affiliation]}</span>
        <br/>
        ${affiliation}
      </p>
    </#list>

    <#if competingInterests?size gt 0>
      <h2>Competing Interests</h2>
      <#list competingInterests as competingInterest>
        <p>${competingInterest}</p>
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


