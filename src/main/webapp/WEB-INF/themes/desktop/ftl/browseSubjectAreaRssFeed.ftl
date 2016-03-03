<#include "common/rssFeedKey.ftl" />

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>
<#if subject?has_content>
  <#assign queryParams = {'sortOrder': selectedSortOrder, 'filterJournals': journalKey, 'unformattedQuery': 'subject:${subject}'}/>
<#else>
  <#assign queryParams = {'sortOrder': selectedSortOrder, 'filterJournals': journalKey, 'unformattedQuery': ''}/>
</#if>
<li id="browseRssFeedButton" class="last">
<a href="<@siteLink handlerName="advancedSearchFeed" queryParameters=queryParams pathVariables={'feedType': 'atom'}/>"
   class="social" title="Get the RSS feed for ${subjectName}">Get the RSS feed for ${subjectName}</a>
</a>
</li>git