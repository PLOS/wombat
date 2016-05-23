<#include '../baseTemplates/base.ftl' />
<#assign cssFile = 'issues.css' />
<#include "../macro/doiResolverLink.ftl" />

<@page_header />
<div id="feedback-form-container">
  <#include "issuesBody.ftl"/>
</div>
<@page_footer />