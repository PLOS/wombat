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
          <input type="image" alt="SEARCH" class="postfix" src="<@siteLink path="resource/img/icon.search.gif" />">
        </div>
      </div>
    </fieldset>
  </form>
  <a id="advSearch" href=""><#-- TODO: address for advanced search controller -->
    advanced search
  </a>
 </li>
