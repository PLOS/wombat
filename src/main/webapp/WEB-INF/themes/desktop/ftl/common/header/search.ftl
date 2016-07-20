<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->

<#macro searchForm journal="">

<li id="navsearch" class="head-search">
  <form name="searchForm" action="<@siteLink handlerName='simpleSearch'/>" method="get">
        <fieldset>
            <legend>Search</legend>
            <label for="search">Search</label>
            <input id="search" type="text" name="q" placeholder="Search" required/>
            <button id="headerSearchButton" type="submit"><span class="search-icon"></span></button>

        </fieldset>
        <#if journal?has_content><input type="hidden" name="filterJournals" value="${journal}"/></#if>
    </form>

    <a id="advSearch"
       href="<@siteLink handlerName='newAdvancedSearch'/>">
      advanced search
    </a>

  </li>

</#macro>
<#include "searchJournal.ftl"/>


<@js src="resource/js/components/placeholder_style.js"/>
