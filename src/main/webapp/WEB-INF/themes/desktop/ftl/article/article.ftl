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

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
  <#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />
    <@displayTabList 'Article' />

    <div class="article-container">

    <#include "nav.ftl" />
    <#include "articleLightbox.ftl" />

      <div class="article-content">


      <#include "revision/revisionNotice.ftl" />
      <#include "amendment.ftl" />

      <#-- Figure carousel is placed here, then inserted midway through article text by JavaScript -->
      <#include "figure_carousel.ftl" />

        <div class="article-text" id="artText">
        ${articleText}

          <div class="ref-tooltip">
             <div class="ref_tooltip-content">

             </div>
          </div>

        </div>
      </div>
    </div>

    </section>
    <aside class="article-aside">
    <#include "aside/sidebar.ftl" />
    </aside>
  </div>

  <#include "../common/footer/footer.ftl" />

  <#include "articleJs.ftl" />
  <@js src="resource/js/components/table_open.js"/>
<#if !showLaterExistsNotice()>
  <@js src="resource/js/components/figshare.js"/>
</#if>
  <#--TODO: move article_lightbox.js to baseJs.ftl when the new lightbox is implemented sitewide -->
  <@js src="resource/js/vendor/jquery.panzoom.min.js"/>
  <@js src="resource/js/vendor/jquery.mousewheel.js"/>

  <@js src="resource/js/components/lightbox.js"/>

  <@js src="resource/js/pages/article_body.js"/>

  <#include "mathjax.ftl">

  <@renderJs />

  <#include "aside/crossmarkIframe.ftl" />
<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
<div class="reveal-modal-bg"></div>
</body>
</html>
