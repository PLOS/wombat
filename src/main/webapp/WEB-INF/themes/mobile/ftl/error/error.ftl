<#include '../baseTemplates/default.ftl' />
<#assign title = 'Server Error' />

<@page_header />
<main class="error">

  <h1>Something's Broken!</h1>

  <p>
    We're sorry, our server has encountered an internal error or misconfiguration and is unable to complete
    your request. This is likely a temporary condition so please try again later.
  </p>

  <p>Thank you for your patience.</p>

  <div class="collapsible" title="+&nbsp;Technical Information for Developers">
    <pre>${stackTrace}</pre>
  </div>
</main>
<@page_footer />
