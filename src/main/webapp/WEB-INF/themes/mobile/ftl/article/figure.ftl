<#include "../common/htmlTag.ftl" />

<#assign title = figure.title />
<#include "../common/head.ftl" />

<body id="page-figure">
<#include "../common/bodyAnalytics.ftl" />

<div id="container-main">
<#include "backToArticle.ftl" />

<#assign imageToShow = figure.thumbnails.large />
  <img class="figure-img" src="asset?id=${imageToShow.file}" alt="${figure.title}">

</div><#--end container main-->

<section id="figure-info-window" class="modal-info-window tab" data-method="tab">
  <a class="modal-tab ">
    More Detail
    <span class="arrow"></span>
  </a>

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content">
    <h3>${figure.title}.</h3>

    <p class="article-id">${figure.doi}</p>

    <div class="figure-description">
    ${figure.descriptionHtml}
    </div>
  </div>

</section><#--end model info window-->

</div><#--end container main-->

<#include "../common/bodyJs.ftl" />
</body>
</html>
