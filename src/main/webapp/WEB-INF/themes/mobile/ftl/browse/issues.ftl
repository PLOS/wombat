<#include '../baseTemplates/base.ftl' />

<#include "../macro/doiResolverLink.ftl" />

<#macro page_content>
<div id="feedback-form-container">
  <#include "issuesBody.ftl"/>
</div>
</#macro>

<@render_page "issues.css" />