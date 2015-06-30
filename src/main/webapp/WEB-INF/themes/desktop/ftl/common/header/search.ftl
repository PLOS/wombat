<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->
<li id="navsearch" class="head-search">
<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>
<form name="searchForm" action="<@siteLink path='search'/>" method="get">
  <fieldset>
    <legend>Search</legend>
    <label for="search">Search</label>
      <input id="search" type="text" name="q" placeholder="Search" required />
      <button type="submit"><span class="search-icon"></span></button>

  </fieldset>
  <input type="hidden" name="filterJournals" value="${journalKey}" />
</form>
<@js src="resource/js/components/placeholder_style.js"/>
