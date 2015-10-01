<#--Use this macro along with the wombat.yaml file to hide/show in-development features. -->
<#macro ifDevFeatureEnabled featureName>
  <@isDevFeatureEnabled feature=featureName ; flag>
    <#if flag>
      <#nested/>
    </#if>
  </@isDevFeatureEnabled>
</#macro>