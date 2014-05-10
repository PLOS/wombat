<div id="db">
  <form name="searchForm" action="search" method="get"><#-- TODO: address for simple search controller -->
    <input type="hidden" name="legacy" value="true" id="legacy"/>
    <fieldset>
      <legend>Search</legend>
      <label for="search">Search</label>

      <div class="wrap">
        <input id="search" type="text" name="q" placeholder="Search">
        <input type="image" alt="SEARCH" src="<@siteLink path="resource/img/icon.search.gif" />">
      </div>
    </fieldset>
  </form>
  <a id="advSearch" href=""><#-- TODO: address for advanced search controller -->
    advanced search
  </a>
</div>
