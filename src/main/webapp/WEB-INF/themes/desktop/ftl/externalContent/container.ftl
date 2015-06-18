<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign cssFile="site-content.css"/>

<#--<#include "../common/customHeaderTags.ftl" />-->

<#--allows for custom tags to be added from the container template-->
<#assign customHeaderTags=[]/>
<#macro addCustomHeaderTag>
  <#assign customHeaderTag>
    <#nested>
  </#assign>
  <#assign customHeaderTags=customHeaderTags + [customHeaderTag] />
</#macro>

<#if externalData??>
  <#if externalData.css_sources??>
    <#list externalData.css_sources as css_source>
      <@addCustomHeaderTag>
        <link rel="stylesheet" type="text/css" href="<@siteLink path='indirect/'/>${css_source}"/>
      </@addCustomHeaderTag> </#list>
  </#if>
</#if>

<@addCustomHeaderTag>
<meta name="feelings" content="I love Johny"/>
</@addCustomHeaderTag>

<@addCustomHeaderTag>

</@addCustomHeaderTag>


<#--allows for custom tags to be added from the container template-->

<#macro printCustomTags>

  <#list customHeaderTags as customTag>
  ${customTag}
  </#list>

</#macro>




<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/header.ftl" />

<#if externalData.content??>
  ${externalData.content}
</#if>


<#if externalData.name??>
  <div data-service="${externalServiceName}" data-${externalData.name}-container/>
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
