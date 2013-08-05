<#macro pathUp steps path>
  <#if steps == 0 > <#-- Without this check, 1..steps is [1, 0], not an empty list. -->
  "${path}"
  <#else>
    <#assign relativePath = path />
    <#list 1..steps as i>
      <#assign relativePath = "../" + relativePath />
    </#list>
  "${relativePath}"
  </#if>
</#macro>
