<#include '../baseTemplates/base.ftl' />

<#macro page_content>
<div class="error">

  <h1>Page Not Found</h1>

  <p>Sorry, the page that you've requested cannot be found; it may have been moved, changed or removed.</p>

  <p>Please use the search form above to locate an article.</p>
</div>
</#macro>

<@render_page '' 'Page Not Found' />

