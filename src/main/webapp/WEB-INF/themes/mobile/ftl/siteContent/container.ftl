<#include '../baseTemplates/base.ftl' />

<#macro page_content>
<div id="content">
  <@fetchHtml type="siteContent" path=siteContentRepoKey/>
</div><#-- end home-content -->
</#macro>

<@render_page />