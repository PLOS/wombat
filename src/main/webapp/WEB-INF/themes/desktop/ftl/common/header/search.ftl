<div id="db">
  <form name="searchForm" action="" method="get"><#-- TODO: address for simple search controller -->
    <input type="hidden" name="from" value="globalSimpleSearch" id="from"/>
    <input type="hidden" name="filterJournals" id="filterJournals"/>
    <fieldset>
      <legend>Search</legend>
      <label for="search">Search</label>

      <div class="wrap">
        <input id="search" type="text" name="query" placeholder="Search">
        <input type="image" alt="SEARCH" src="<@siteLink path="resource/img/icon.search.gif" />">
      </div>
    </fieldset>
  </form>
  <a id="advSearch" href=""><#-- TODO: address for advanced search controller -->
    advanced search
  </a>
</div>
