<#include "citation.ftl" />
<#macro amendmentNotice amendmentObjects type title linkText showCount>
<div class="amendment amendment-${type} toc-section">
  <#assign tocTitle>
    <#t/>${title}<#if showCount> (${amendmentObjects?size})</#if>
  </#assign>
  <a data-toc="amendment-${type}" title="${tocTitle}" id="amendment-${type}" name="amendment-${type}"></a>

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
            <span class="link-separator"> </span>
            <a href="${path + amendment.doi}" class="amendment-link">
            ${linkText}</a>
          </@siteLink>
        </#if>
      </p>
    </div>
  </#list>
</div>
</#macro>

<#if amendments.correction??>
  <@amendmentNotice amendments.correction, "correction",
  (amendments.correction?size == 1)?string("Correction", "Corrections"),
  "View correction", true />
</#if>
<#if amendments.eoc??>
  <@amendmentNotice amendments.eoc "eoc" "Expression of Concern" "View expression of concern" false />
</#if>
<#if amendments.retraction??>
  <@amendmentNotice amendments.retraction "retraction" "Retraction" "View retraction" false />
</#if>
