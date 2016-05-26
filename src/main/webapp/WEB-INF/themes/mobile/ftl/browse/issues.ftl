<#include '../baseTemplates/default.ftl' />
<#assign cssFile = 'issues.css' />
<#include "../macro/doiResolverLink.ftl" />

<@page_header />
<main id="feedback-form-container">
  <#include "issuesBody.ftl"/>
</main>
<@page_footer />