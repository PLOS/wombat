<#include '../baseTemplates/default.ftl' />
<#assign title = 'Page Not Found' />
<#assign mainClass = "error" />

<@page_header />

<h1>Page Not Found</h1>

<p>Looking for an article? Use the article search box above, or try <a href="<@siteLink handlerName="advancedSearch" />">advanced search form</a>.</p>

<p>Need information about the journal or about submitting a manuscript? Use the Publish and About menus above, <a
    href="//plos.org/search">or
  search the PLOS websites</a>. You can also <@siteLink handlerName="siteContent" pathVariables={"pageName": "contact"} ; href><a href="${href}">contact the journal office</a> </@siteLink>.</p>

<p>Experiencing an issue with the website? Please use the <a href="<@siteLink handlerName="feedback" />">feedback form</a> and provide a detailed description of the
  problem.</p>
<@page_footer />

