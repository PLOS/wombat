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
<#assign title = figure.title />

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

  <section class="article-body">
    <a class="back" href="<@siteLink path="article?id=${article.doi}" />">Back to Article</a>

    <h1 id="artTitle"><@xform xml=article.title/></h1>



    <div class="article-container">


        <h2>${figure.title}</h2>

          <img class="figure-img"
               src="<@siteLink handlerName="assetFile" queryParameters=(figurePtr + {'type': 'large'}) />"
               alt="${figure.title}">

            <p class="article-id">${figure.doi}</p>

            <div class="figure-description">
            ${descriptionHtml}
            </div>

        </div>
    </div>


</div>

<#include "../common/footer/footer.ftl" />

<#include "articleJs.ftl" />



<@renderJs />

<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
</body>
</html>
