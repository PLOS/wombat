<#include "../baseTemplates/articleSection.ftl" />
<#assign title = article.title />
<#assign bodyId = 'page-figures' />
<#assign mainId = "figures-content" />
<#assign mainClass = "content" />

<@page_header />
<#list article.figures as figure>
  <#assign hasTitle = figure.title?? && figure.title?length gt 0 />
<figure class="figure-small">
  <figcaption>
    <#if hasTitle>
    ${figure.title}.
    </#if>
    <div class="figure-description">
    ${figure.descriptionHtml}
    </div>
    <a href="figure?id=${figure.doi}">More »</a>
  </figcaption>

  <a class="figure-link" href="figure?id=${figure.doi}">
    <img class="figure-image" src="asset?id=${figure.thumbnails.medium.file}"
         alt="<#if hasTitle>${figure.title}</#if>"> <#-- Print alt="" if no title -->
    <span class="figure-expand">Expand</span>
  </a>

</figure>
</#list>
<#include "../common/bottomMenu/bottomMenu.ftl" />
<@page_footer />
