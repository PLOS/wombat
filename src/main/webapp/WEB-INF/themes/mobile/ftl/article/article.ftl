<#include "../baseTemplates/article.ftl" />
<#assign title = article.title />
<#assign mainId = "articleText" />
<#assign isArticlePage = true />

<@page_header />
${articleText}
<@page_footer />
