<div class="search-results-controls">
  <div class="search-results-controls-first-row">
    <fieldset class="search-field">
      <legend>Search</legend>
      <label for="controlBarSearch">Search</label>
      <input id="controlBarSearch" type="text" pattern=".{1,}" name="q"
             value="${query}" required/>
      <button id="searchFieldButton" type="submit">
        <i class="search-icon"></i>
      </button>
      <i title="Clear Search Input" class="icon-times-circle clear"></i>
    </fieldset>
    <a id="advancedSearchLink" class="advanced-search-toggle-btn" href="#">Advanced Search</a>
    <a id="simpleSearchLink" class="advanced-search-toggle-btn" href="#">Simple Search</a>
    <a class="edit-query" href="#">Edit Query</a>
  </div>
  <div class="advanced-search-container">
  </div>

</div>
<#include "advancedSearchQueryBuilder.ftl" />