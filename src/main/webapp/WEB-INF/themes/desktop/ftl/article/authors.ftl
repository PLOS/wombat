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

  <section class="article-body">

    <#include "tabs.ftl" />
    <@displayTabList 'authors' />
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
      <#else>
        <h2>Corresponding Authors</h2>
      </#if>
      <#list correspondingAuthors as correspondingAuthor>
        <p class="about-author">${correspondingAuthor}</p>
      </#list>
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

<@js src="resource/js/components/show_onscroll.js"/>

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/vendor/moment.js"/>

<@js src="resource/js/components/twitter_module.js"/>
<@js src="resource/js/components/signposts.js"/>
<@js src="resource/js/vendor/spin.js"/>

<@js src="resource/js/pages/article.js"/>
<@js src="resource/js/pages/article_sidebar.js"/>
<@renderJs />




<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
<script type="text/javascript"
        src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script type="text/javascript"
        src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

<#include "aside/crossmarkIframe.ftl" />
<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
<div class="reveal-modal-bg"></div>
<div id="article-lightbox" class="reveal-modal" data-reveal>

</div>
</body>
</html>


