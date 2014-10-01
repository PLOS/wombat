<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->
<li id="navsearch" class="head-search">

<#macro searchForm>
  <form name="searchForm" action="search" method="get">
    <#nested/>
    <fieldset>
      <legend>Search</legend>
      <label for="search">Search</label>
          <input id="search" type="text" name="q" placeholder="Search" required />
          <button type="submit"><span class="search-icon"></span></button>

    </fieldset>
  </form>
</#macro>

<@themeConfig map="legacy" value="urlPrefix" ; legacyUrlPrefix>
  <#if legacyUrlPrefix??>
  <#-- Point at a legacy Ambra instance for search results -->
    <@searchForm>
    <#-- This constant signals the controller to resolve it as a legacy search -->
      <input type="hidden" name="legacy" value="true" id="legacy"/>
    </@searchForm>
    <@themeConfig map="journal" value="journalKey" ; filterJournal>
      <a id="advSearch"
         href="${legacyUrlPrefix}search/advanced?noSearchFlag=true&query=&filterJournals=${filterJournal}">
        advanced search
      </a>
    </@themeConfig>
  <#else>
  <#-- Point at our own simple search controller -->
    <@searchForm/>
  </#if>
</@themeConfig>
</li>

<@js src="resource/js/components/placeholder_style.js"/>
