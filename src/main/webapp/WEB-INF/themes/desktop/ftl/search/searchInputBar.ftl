<div class="search-results-controls">
    <div class="search-results-controls-first-row">
        <fieldset class="search-field">
            <legend>Search</legend>
            <label for="controlBarSearch">Search</label>
            <input id="controlBarSearch" type="text" pattern=".{1,}" name="${advancedSearch?string('unformattedQuery', 'q')}"
                   value="${query}" required/>
        <#list activeFilterItems as item>
            <input type="hidden" name="${item.filterParamName}" value="${item.filterValue}"/>
        </#list>
        <#if (filterStartDate??)>
            <input type="hidden" name="filterStartDate" value="${filterStartDate}"/>
            <#if (filterEndDate??)>
                <input type="hidden" name="filterEndDate" value="${filterEndDate}"/>
            </#if>
        </#if>
        <@ifDevFeatureEnabled 'advancedSearch'>
          <button id="searchFieldButton" type="submit" class="toggle-icons">
              <i class="search-icon"></i>
              <i title="Clear Search Input" class="fa fa-times-circle fa-lg clear"></i>
            </button>
        </fieldset>
        <a id="advancedSearchLink" class="advanced-search-toggle-btn" href="#">Advanced Search</a>
        <a id="simpleSearchLink" class="advanced-search-toggle-btn" href="#">Simple Search</a>
        <a class="edit-query" href="#">edit query</a>
    </@ifDevFeatureEnabled>
    <@ifDevFeatureDisabled 'advancedSearch'>
          <button id="searchFieldButton" type="submit">
            <i class="search-icon"></i>
          </button>
      </fieldset>
      <a id="advancedSearchLink" class="search-results-advanced-search-submit" href="${advancedSearchLink}">Advanced
        Search</a>
    </@ifDevFeatureDisabled>
    </div>
    <@ifDevFeatureEnabled 'advancedSearch'>
    <div class="advanced-search-container">
    </div>
    </@ifDevFeatureEnabled>
</div>
<#include "advancedSearchQueryBuilder.ftl" />