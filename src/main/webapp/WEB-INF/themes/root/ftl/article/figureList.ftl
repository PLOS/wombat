<#list figures as figure>
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
      <@siteLink handlerName="figureImage" queryParameters={"size": "medium", "id": figure.doi} ; src>
        <img class="figure-image" src="${src}"
             alt="<#if hasTitle>${figure.title}</#if>"> <#-- Print alt="" if no title -->
        <span class="figure-expand">Expand</span>
      </@siteLink>
    </a>
    
  </figure>
</#list>