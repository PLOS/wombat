<#include "../baseTemplates/articleFigure.ftl" />

<#assign title = figure.title />

<@page_header />
<img class="figure-img"
     src="<@siteLink handlerName="assetFile" queryParameters=(figurePtr + {'type': 'large'}) />"
     alt="${figure.title}">

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
    ${descriptionHtml}
    </div>
  </div>

</section><#--end model info window-->
<@page_footer />
