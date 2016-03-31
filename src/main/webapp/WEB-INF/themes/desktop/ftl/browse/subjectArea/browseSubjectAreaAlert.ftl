<#if subject??><#-- Don't show alert button on "All Subject Areas" page -->
<li id="browseJournalAlertButton" class="middle">
  <a href="#" title="Get an email alert for ${subjectName}"
    <#if isUserLoggedIn()>
     id="save-journal-alert-link" class="journal-alert <#if subscribed>subscribed</#if>"
    <#else>
     id="login-link"
    </#if>
     data-category="${subjectName}">
    Get an email alert for ${subjectName}
  </a>
</li>
</#if>
