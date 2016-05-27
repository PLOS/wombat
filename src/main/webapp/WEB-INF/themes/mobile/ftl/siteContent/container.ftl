<#include '../baseTemplates/default.ftl' />
<#assign mainId = "content" />

<@page_header />
<@fetchHtml type="siteContent" path=siteContentRepoKey/>
<@page_footer />