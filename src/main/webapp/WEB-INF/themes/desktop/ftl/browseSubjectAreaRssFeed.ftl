<#include "common/rssFeedKey.ftl" />

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>

<li id="browseJournalAlertButton" class="middle">
  <#if isUserLoggedIn()>
    <#if subscribed>
      <#assign subscribedClass = " subscribed">
    <#else>
      <#assign subscribedClass = "">
    </#if>
    <a href="#" title="Get an email alert for ${subject}" class="journal-alert${subscribedClass}" id="save-journal-alert-link" data-category="${subject}">Get an email alert for ${subject}</a>
  <#else>
    <a href="#" title="Get an email alert for ${subject}" id="login-link" data-category="${subject}">Get an email alert for ${subject}</a>
  </#if>
</li>

<#if subject?has_content>
  <#assign subjectParam = 'subject:"${subject?replace("_"," ")}"'/>
</#if>
<#assign queryParams = {'sortOrder': selectedSortOrder, 'filterJournals': journalKey, 'unformattedQuery': subjectParam!''}/>
<li id="browseRssFeedButton" class="last">
  <a href="<@siteLink handlerName="advancedSearchFeed" queryParameters=queryParams pathVariables={'feedType': 'atom'}/>"
     class="social" title="Get the RSS feed for ${subjectName}">Get the RSS feed for ${subjectName}</a>
  </a>
</li>