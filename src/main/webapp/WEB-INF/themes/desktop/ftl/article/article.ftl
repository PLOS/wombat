<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

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
<#assign isArticlePage = true />

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
  <#if !hasLaterVersion()>
    <#include "../common/figshareJs.ftl" />
  </#if>
  <#--TODO: move article_lightbox.js to baseJs.ftl when the new lightbox is implemented sitewide -->
  <@js src="resource/js/vendor/jquery.panzoom.min.js"/>
  <@js src="resource/js/vendor/jquery.mousewheel.js"/>

  <@js src="resource/js/components/lightbox.js"/>

  <@js src="resource/js/pages/article_body.js"/>

  <#include "mathjax.ftl">

  <@renderJs />
<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
<div class="reveal-modal-bg"></div>
</body>
</html>
