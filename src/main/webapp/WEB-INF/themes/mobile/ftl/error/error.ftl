<#include '../baseTemplates/base.ftl' />

<#macro page_content>
<div class="error">

  <h1>Something's Broken!</h1>

  <p>
    We're sorry, our server has encountered an internal error or misconfiguration and is unable to complete
    your request. This is likely a temporary condition so please try again later.
  </p>

  <p>Thank you for your patience.</p>

  <div class="collapsible" title="+&nbsp;Technical Information for Developers">
    <pre>${stackTrace}</pre>
  </div>
</div>
</#macro>

<@render_page '' 'Server Error' />
