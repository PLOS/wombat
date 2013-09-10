<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Figures" /><#--TODO-->
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-figures">
<div id="container-main">
<#include "backToArticle.ftl" />

  <section id="figures-content" class="content">
  <#list article.figures as figure>
    <figure class="figure-small">

      <figcaption>
      ${figure.title}.
      ${figure.descriptionHtml}
        <a href="figure?id=${article.doi}">More »</a>
      </figcaption>

      <a class="figure-link" href="figure?id=${figure.doi}">
        <img class="figure-image" src="asset?id=${figure.thumbnails.medium.file}"
             alt="${figure.title}">
        <span class="figure-expand">Expand</span>
      </a>

    </figure>
  </#list>

  </section><#--end content-->

<#include "../common/bottomMenu/bottomMenu.ftl" />

<#include "../common/footer/footer.ftl" />

</div>
<!--end container main-->

<#include "../common/bodyJs.ftl" />
</body>
</html>
