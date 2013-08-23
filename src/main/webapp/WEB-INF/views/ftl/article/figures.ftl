<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Figures" /><#--TODO-->
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-figures">
<div id="container-main">
<#include "backToArticle.ftl" />

  <section id="figures-content" class="content">
  <#list figures as figure>
    <figure class="figure-small">

      <figcaption>
      ${figure.original.title}.
      ${figure.original.description} <#-- TODO: This is untransformed XML; should be transformed to HTML. -->
        <a href="figure?id=${article.doi}">More »</a>
      </figcaption>

      <a class="figure-link" href="figure?id=${figure.id}">
        <img class="figure-image" src="asset?id=${figure.thumbnails[0].doi}.${figure.thumbnails[0].extension}"
             alt="${figure.original.title}">
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
