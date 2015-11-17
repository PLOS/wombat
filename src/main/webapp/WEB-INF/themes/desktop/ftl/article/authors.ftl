<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign tabPage = "Authors" />


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
        <div class="article-container">
            <h1>About the Authors</h1>
        <#list authors as author>
            <div class="about-author">
                <h5 class="comments-header">${author.fullName}</h5>   <p>


              <#list author.affiliations as affiliation>
              ${affiliation}

                <#if author.affiliations?size gt 0>
                  <br/>
              </#if>
              </#list>
                </p>
            </div>
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

        </div>

    </section>
    <aside class="article-aside">
    <#include "aside/sidebar.ftl" />
    </aside>
</div>

<#include "../common/footer/footer.ftl" />

<@js src="resource/js/components/show_onscroll.js"/>

<#--TODO: move article_lightbox.js to baseJs.ftl when the new lightbox is implemented sitewide -->
<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/vendor/moment.js"/>

<@js src="resource/js/components/twitter_module.js"/>
<@js src="resource/js/components/signposts.js"/>
<@js src="resource/js/vendor/spin.js"/>

<@js src="resource/js/pages/article_sidebar.js"/>
<@renderJs />




<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js" ></script>
<script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

<#include "aside/crossmarkIframe.ftl" />
<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
<div class="reveal-modal-bg"></div>
<div id="article-lightbox" class="reveal-modal" data-reveal>

</div>
</body>
</html>


