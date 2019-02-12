<#list figures as figure>
  <#assign hasTitle = figure.title?? && figure.title?length gt 0 />
  <figure class="figure-small">
    <figcaption>
      <#if hasTitle>
      <h3 class="figure-title">
      ${figure.title}.
      </h3>
      </#if>
      <p class="figure-description">
      ${figure.descriptionHtml}
      </p>
      <p><a href="figure?id=${figure.doi}">More &raquo;</a></p>
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