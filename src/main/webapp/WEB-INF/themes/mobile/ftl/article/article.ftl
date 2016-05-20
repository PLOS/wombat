<#include "../baseTemplates/article.ftl" />

<#macro page_content>
<div id="articleText">
${articleText}
</div>
</#macro>

<@render_page '' article.title />