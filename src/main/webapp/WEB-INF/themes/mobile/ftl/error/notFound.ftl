<#include '../baseTemplates/default.ftl' />
<#assign title = 'Page Not Found' />
<#assign mainClass = "error" />

<@page_header />

<h1>Page Not Found</h1>

<p>Looking for an article? Use the article search box above, or try <a href="<@siteLink handlerName="advancedSearch" />">advanced search form</a>.</p>

<p>Experiencing an issue with the website? Please use the <a href="<@siteLink handlerName="feedback" />">feedback form</a> and provide a detailed description of the
  problem.</p>
<@page_footer />

