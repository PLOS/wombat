<#if article.figures?has_content>
<div>

  <#list article.figures as figure>

  <#-- Omit figures that don't appear in article body (e.g. a striking image) -->
  <#if figure.contextElement?has_content>

    <@siteLink path=("article/figure/image?size=small&id=" + figure.doi) ; src>
    <a href="#" class="lightbox-figure" data-figure-doi="${figure.doi}">
      <img src="${src?html}"
      <#if figure.title?has_content >
        alt="${figure.title?html}"
      </#if>
      />
    </a>
    </@siteLink>

  </#if>
  </#list>
</div>

<div id="article-lightbox" class="reveal-modal full" data-reveal aria-labelledby="modalTitle" aria-hidden="true" role="dialog">
    <img src="" />
</div>
</#if>


