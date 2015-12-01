<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>

<#macro searchForm journal="${journalKey}">

<li id="navsearch" class="head-search">
    <form name="searchForm" action="<@siteLink path='search'/>" method="get">
        <fieldset>
            <legend>Search</legend>
            <label for="search">Search</label>
            <input id="search" type="text" name="q" placeholder="Search" required/>
            <button id="headerSearchButton" type="submit"><span class="search-icon"></span></button>

        </fieldset>
        <input type="hidden" name="filterJournals" value="${journal}"/>
    </form>

  <@themeConfig map="legacy" value="urlPrefix" ; legacyUrlPrefix>
    <#if legacyUrlPrefix??>
      <a id="advSearch"
         href="${legacyUrlPrefix}search/advanced?noSearchFlag=true&query=&filterJournals=${journalKey}">
          advanced search
      </a>
    </#if>
  </@themeConfig>
</li>

</#macro>
<#include "searchJournal.ftl"/>


<@js src="resource/js/components/placeholder_style.js"/>
