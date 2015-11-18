<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign cssFile="metrics.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
    <#include "articleHeader.ftl" />
    <section class="metrics-body">
        <#include "tabs.ftl" />
        <#include "metricsBody.ftl" />
    </section>
    <aside class="article-aside">
        <#include "aside/sidebar.ftl" />
    </aside>
  </div>

  <#include "../common/footer/footer.ftl" />

  <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.js" ></script>

  <@js src="resource/js/components/show_onscroll.js"/>
  <@js src="resource/js/components/table_open.js"/>
  <@js src="resource/js/components/figshare.js"/>
  <@js src="resource/js/components/tooltip_hover.js"/>

  <@js src="resource/js/util/alm_config.js"/>
  <@js src="resource/js/util/alm_query.js"/>
  <@js src="resource/js/vendor/moment.js"/>
  <@js src="resource/js/vendor/jquery.jsonp-2.4.0.js"/>
  <@js src="resource/js/vendor/hover-enhanced.js"/>
  <@js src="resource/js/highcharts.js"/>

  <@js src="resource/js/components/twitter_module.js"/>
  <@js src="resource/js/components/signposts.js"/>
  <@js src="resource/js/components/nav_builder.js"/>
  <@js src="resource/js/components/floating_nav.js"/>

  <@js src="resource/js/vendor/spin.js"/>

  <@js src="resource/js/pages/alm.js"/>
  <@js src="resource/js/pages/article.js"/>
  <@js src="resource/js/pages/article_body.js"/>
  <@js src="resource/js/pages/article_sidebar.js"/>
  <@renderJs />


  <script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
  <script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

  <#include "aside/crossmarkIframe.ftl" />

</body>
</html>
