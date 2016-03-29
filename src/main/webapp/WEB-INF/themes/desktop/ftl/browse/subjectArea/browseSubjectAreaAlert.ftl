<li id="browseJournalAlertButton" class="middle">
<#if isUserLoggedIn()>
  <#if subscribed>
    <#assign subscribedClass = " subscribed">
  <#else>
    <#assign subscribedClass = "">
  </#if>
  <a href="#" title="Get an email alert for ${subject}" class="journal-alert${subscribedClass}"
     id="save-journal-alert-link" data-category="${subject}">Get an email alert for ${subject}</a>
<#else>
  <a href="#" title="Get an email alert for ${subject}" id="login-link" data-category="${subject}">Get an email alert
    for ${subject}</a>
</#if>
</li>
