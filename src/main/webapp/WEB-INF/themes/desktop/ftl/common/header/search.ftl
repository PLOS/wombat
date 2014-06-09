<@themeConfig map="legacy" value="urlPrefix" ; p>
  <#assign prefix = p />
</@themeConfig>

<@themeConfig map="journal" value="journalKey" ; j>
  <#assign filterJournal = j />
</@themeConfig>

<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->
<li id="search" class="head-search">
  <form name="searchForm" action="search" method="get"><#-- TODO: address for simple search controller -->
    <input type="hidden" name="legacy" value="true" id="legacy"/>
    <fieldset>
      <legend>Search</legend>
      <label for="search">Search</label>

      <div class="row collapse">
        <div class=" wrap small-8 columns">
          <input id="search" type="text" name="q" placeholder="Search">
        </div>
        <div class="small-4 columns">
          <button type="submit"><span class="search-icon"></span></button>
        </div>
      </div>
    </fieldset>
  </form>
  <#-- the link will be replaced with the correct advanced search after the migration -->
  <#-- TODO: address for advanced search controller -->
  <a id="advSearch" href="${prefix}search/advanced?noSearchFlag=true&query=&filterJournals=${filterJournal}">
    advanced search
  </a>
</li>
