<#--
  Defines a macro for printing the tab links at the top of an article page.
  The invoking page should call displayTabList, passing the current page. (tabyLink is private)
  -->

<#macro tabyLink tabPage section handlerName>
  <@themeConfig map="mappings" value=handlerName ; mappingFlag><#if !(mappingFlag?? && mappingFlag.disabled?? && mappingFlag.disabled)>
  <li class="tab-title <#if tabPage == section>active</#if>" id="tab${section}">
    <#assign tabCounter = tabCounter + 1 />
    <@siteLink handlerName=handlerName queryParameters={"id": article.doi} ; href>
      <a href="${href}" class="article-tab-${tabCounter?c}"><#nested/></a>
    </@siteLink>
  </li>
  </#if></@themeConfig>
</#macro>

<#macro displayTabList tabPage='Article'>
<ul class="article-tabs">
  <#assign tabCounter = 0 /> <#-- Referred to privately by tabyLink -->

  <@tabyLink tabPage "Article" "article">Article</@tabyLink>

  <#if authors?? && authors?size gt 0>
    <#if !["Correction", "Retraction", "Expression of Concern", "Obituary"]?seq_contains(articleTypeHeading) >
      <@tabyLink tabPage "Authors" "articleAuthors">Authors</@tabyLink>
    </#if>
  </#if>

  <@tabyLink tabPage "Metrics" "articleMetrics">Metrics</@tabyLink>
  <@tabyLink tabPage "Comments" "articleComments">Comments</@tabyLink>
  <@tabyLink tabPage "Related" "articleRelatedContent">Related Content</@tabyLink>
</ul>
</#macro>
