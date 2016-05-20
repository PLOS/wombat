<#include '../baseTemplates/base.ftl' />

<#assign articleDoi = article.doi />
<#include "../macro/doiResolverLink.ftl" />

<#macro page_content>
  <#include  "emailArticleBody.ftl" />
</#macro>

<@render_page 'feedback.css' article.title />
