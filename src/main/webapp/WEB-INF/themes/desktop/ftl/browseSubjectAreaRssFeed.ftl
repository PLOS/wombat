<#include "common/rssFeedKey.ftl" />
<#if selectedSortOrder == "DATE_NEWEST_FIRST">
<#--Sort order in ambra is different, change it to what it used to be-->
  <#assign legacySelectedSortOrder = "Date%2C+newest+first"/>
<#else>
  <#assign legacySelectedSortOrder = "Most+views%2C+all+time"/>
</#if>
<li id="browseRssFeedButton" class="last">
<#if subject?has_content>
  <a href="<@siteLink handlerName="browseFeed" pathVariables={'feedType': 'atom', 'subject': subject}/>"
     class="social" title="Get the RSS feed for ${subjectName}">Get the RSS feed for ${subjectName}</a>
  </a>
<#else >
  <a href="<@siteLink handlerName="browseAllFeed" pathVariables={'feedType': 'atom'}/>"
     class="social" title="Get the RSS feed for ${subjectName}">Get the RSS feed for ${subjectName}</a>
  </a>
</#if>
</li>