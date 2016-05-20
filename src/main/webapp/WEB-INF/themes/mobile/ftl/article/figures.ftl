<#include "../baseTemplates/articleSection.ftl" />
<#macro page_content>
<section id="figures-content" class="content">
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

</section><#--end content-->

  <#include "../common/bottomMenu/bottomMenu.ftl" />
</#macro>

<@render_page '' article.title 'page-figures' />
