<@themeConfig map="legacy" value="urlPrefix" ; p>
  <#assign prefix = p />
</@themeConfig>

<@themeConfig map="journal" value="journalKey" ; j>
  <#assign filterJournal = j />
</@themeConfig>

<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->
<li id="navsearch" class="head-search">
  <form name="searchForm" action="search" method="get"><#-- TODO: address for simple search controller -->
    <input type="hidden" name="legacy" value="true" id="legacy"/>
    <fieldset>
      <legend>Search</legend>
      <label for="search">Search</label>



          <input id="search" type="text" name="q" placeholder="Search" required />

          <button type="submit"><span class="search-icon"></span></button>

    </fieldset>
  </form>
  <a id="advSearch" href="${prefix}search/advanced?noSearchFlag=true&query=&filterJournals=${filterJournal}">
    advanced search
  </a>
</li>
