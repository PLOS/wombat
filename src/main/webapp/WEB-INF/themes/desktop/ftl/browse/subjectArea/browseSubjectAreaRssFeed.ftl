<#include "../../common/rssFeedKey.ftl" />

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>
<#if subject?has_content>
  <#assign subjectParam = 'subject:"${subject?replace("_"," ")}"'/>
</#if>
<#assign queryParams = {'sortOrder': selectedSortOrder, 'filterJournals': journalKey, 'unformattedQuery': subjectParam!''}/>
<li id="browseRssFeedButton" class="last">
  <a href="<@siteLink handlerName="advancedSearchFeed" queryParameters=queryParams pathVariables={'feedType': 'atom'}/>"
     class="social" title="Get the RSS feed for ${subjectName}">Get the RSS feed for ${subjectName}</a>
  </a>
</li>