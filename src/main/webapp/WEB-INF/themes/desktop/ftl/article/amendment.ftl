<#include "citation.ftl" />
<#macro amendmentNotice amendmentObjects type title linkText>
<div id="article-amendment-${type}">
  <h3 class="${type}">${title}</h3>
  <#list amendmentObjects as amendment>
    <div class="amendment-body">
    ${amendment.body}
    </div>
    <div class="amendment-citation">
      <@displayCitation amendment/>
      |
      <@siteLink path="article?id=" ; path>
        <a href="${path + amendment.doi}">${linkText}</a>
      </@siteLink>
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
