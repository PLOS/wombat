<#include "citation.ftl" />

<#if revisionMenu?size gt 1> <!-- add if uncorrected proof -->
<div class="amendment amendment-uncorrected-proof">

<#if article.publicationStage?? && article.publicationStage == "uncorrected-proof"><h2>This is an uncorrected proof</h2></#if>

<p>
<#if revisionMenu?size?number gt articlePtr.rev?number>You are currently viewing an older version of this article.
  A <a href="<@siteLink handlerName="article" queryParameters={"id" : article.doi} />">new version</a> is available.</#if>
</p>

</div>
</#if>

<#macro amendmentNotice amendmentGroup title linkText index>
<div class="amendment amendment-${amendmentGroup.type} toc-section">
  <#assign tocTitle>
    <#t/>${title}<#if amendmentGroup.amendments?size gt 1> (${amendmentGroup.amendments?size})</#if>
  </#assign>
  <a data-toc="amendment-${index?c}" title="${tocTitle}" id="amendment-${index?c}" name="amendment-${index?c}"></a>

  <h2>${title}</h2>
  <#list amendmentGroup.amendments as amendment>
    <#if amendment.body??>
      <div class="amendment-body">
      ${amendment.body}
      </div>
    </#if>
    <div class="amendment-citation">
      <p>
        <#if amendment.publicationDate??>
          <span class="amendment-date">
            <@formatJsonDate date="${amendment.publicationDate}" format="d MMM yyyy" />:
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

<#list amendments as amendmentGroup>
  <#if amendmentGroup.type == 'correction'>
    <@amendmentNotice amendmentGroup,
    (amendmentGroup.amendments?size == 1)?string("Correction", "Corrections"),
    "View correction" amendmentGroup_index />
  </#if>
  <#if amendmentGroup.type == 'eoc'>
    <@amendmentNotice amendmentGroup "Expression of Concern" "View expression of concern" amendmentGroup_index />
  </#if>
  <#if amendmentGroup.type == 'retraction'>
    <@amendmentNotice amendmentGroup "Retraction" "View retraction" amendmentGroup_index />
  </#if>
</#list>
