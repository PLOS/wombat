<#include "../baseTemplates/article.ftl" />
<#assign title = article.title />
<#assign mainId = "articleText" />


<@page_header />
${articleText}
<@page_footer />
