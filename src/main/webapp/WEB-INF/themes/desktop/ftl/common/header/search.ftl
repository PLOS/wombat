<div id="search" class="head-search">
  <form name="searchForm" action="" method="get"><#-- TODO: address for simple search controller -->
    <input type="hidden" name="from" value="globalSimpleSearch" id="from"/>
    <input type="hidden" name="filterJournals" id="filterJournals"/>
    <fieldset>
      <legend>Search</legend>
      <label for="search">Search</label>

      <div class="wrap row collapse">
         <div class="small-8 columns">
        <input id="search" type="text" name="query" placeholder="Search">
         </div>
        <div class="small-4 columns">
        <input type="image" alt="SEARCH" class="postfix" src="${pathUp(depth!0 "resource/img/icon.search.gif")}">    </div>
      </div>
    </fieldset>
  </form>
  <a id="advSearch" href=""><#-- TODO: address for advanced search controller -->
    advanced search
  </a>
</div>
