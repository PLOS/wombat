<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign cssFile="site-content.css"/>
<#include "../common/customHeadTags.ftl" />

<#if externalData??>
  <#if externalData.css_sources??>
    <#list externalData.css_sources as css_source>
      <@addCustomHeadTag>
      <link rel="stylesheet" type="text/css" href="<@siteLink path='indirect/'/>${css_source}"/>
      </@addCustomHeadTag> </#list>
  </#if>
</#if>

<@addCustomHeadTag>
<meta name="asset-url-prefix" content="/lala/indirect/">
</@addCustomHeadTag>

<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<#if externalData.content??>
  ${externalData.content}
</#if>


<#if externalData.name??>
  <div id="external-content-container" data-service="${externalServiceName}" data-${externalData.name}-container/>
</#if>

<#include "../common/footer/footer.ftl" />

<@renderJs />

<#if externalData.js_sources??>
  <#list externalData.js_sources as js_source>
    <script src="<@siteLink path='indirect/'/>${js_source}" type="text/javascript"></script>
  </#list>
</#if>


</body>
</html>
