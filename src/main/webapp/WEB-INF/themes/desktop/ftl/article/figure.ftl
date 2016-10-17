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
n
<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
<#--<#include "articleHeader.ftl" />-->
  <section class="article-body">

  <#--<#include "tabs.ftl" />-->

    <div class="article-container">

      <div class="article-content">

      <#-- Figure carousel is placed here, then inserted midway through article text by JavaScript -->

        <div class="article-text" id="artText">


        </div>
      </div>
    </div>

  </section>

</div>

<#include "../common/footer/footer.ftl" />

<#include "articleJs.ftl" />


<@js src="resource/js/pages/article_body.js"/>


<@renderJs />

<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
</body>
</html>
