<#if article.figures?has_content>
<div>

  <#list article.figures as figure>

  <#-- Omit figures that don't appear in article body (e.g. a striking image) -->
  <#if figure.contextElement?has_content>

    <@siteLink path=("article/figure/image?size=large&id=" + figure.doi) ; src>
      <img src="${src?html}"
      <#if figure.title?has_content >
        alt="${figure.title?html}"
      </#if>
      />
    </@siteLink>

  </#if>
  </#list>
</div> 
</#if>


