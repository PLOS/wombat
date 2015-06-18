<#--allows for custom tags to be added from the container template-->
<#--add a new custom tag to container.ftl by using the following example BEFORE the head.ftl include
<@addCustomHeadTag>
<meta name="asset-url-prefix" content="/lala/indirect/">
</@addCustomHeadTag>
-->

<#assign customHeadTags=[]/>
<#macro addCustomHeadTag>
  <#assign customHeadTag>
    <#nested>
  </#assign>
  <#assign customHeadTags=customHeadTags + [customHeadTag] />
</#macro>

<#macro printCustomTags>
  <#list customHeadTags as customTag>
  ${customTag}
  </#list>
</#macro>



