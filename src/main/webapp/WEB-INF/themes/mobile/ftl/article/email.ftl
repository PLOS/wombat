<#include '../baseTemplates/default.ftl' />

<#assign articleDoi = article.doi />
<#assign title = article.title />
<#assign cssFile = 'feedback.css' />
<#include "../macro/doiResolverLink.ftl" />

<@page_header />
<#include  "emailArticleBody.ftl" />
<@page_footer />
