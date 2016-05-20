<#include '../baseTemplates/base.ftl' />

<#macro page_content>
<div class="error">
  <h1>Something's Broken!</h1>

  <p>We're sorry, user data and comments are unavailable. This is likely a temporary condition so please try again later.</p>
  <p>Thank you for your patience.</p>
</div>
</#macro>

<@render_page '' 'Server error' />

