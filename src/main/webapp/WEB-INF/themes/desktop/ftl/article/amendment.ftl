<#include "citation.ftl" />
<#macro amendmentNotice amendmentObjects type title linkText>
<div class="amendment amendment-${type}" id="amendment-${type}">
  <h2>${title}</h2>
  <#list amendmentObjects as amendment>
    <#if amendment.body??>
      <div class="amendment-body">
      ${amendment.body}
      </div>
    </#if>
    <div class="amendment-citation">
      <p>
      <#if amendment.date??>
        <span class="amendment-date">
        <@formatJsonDate date="${amendment.date}" format="d MMM yyyy" />:
      </span>
      </#if>

      <@displayCitation amendment false />

      <#if amendment.doi??>

        <@siteLink path="article?id=" ; path>
          <a href="${path + amendment.doi}" class="amendment-link">${linkText}</a>
        </@siteLink>
      </#if>
      </p>
    </div>
  </#list>
</div>
</#macro>

<#if amendments.correction??>
  <@amendmentNotice amendments.correction "correction" "Corrections" "View correction" />
</#if>
<#if amendments.eoc??>
  <@amendmentNotice amendments.eoc "eoc" "Expression of Concern" "View expression of concern" />
</#if>
<#if amendments.retraction??>
  <@amendmentNotice amendments.retraction "retraction" "Retraction" "View retraction" />
</#if>
