<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign title = "Search Results" />
<#assign cssFile="search-results.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../macro/searchResultsAlm.ftl" />

<@js src="resource/js/util/class.js"/>
<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/components/pagination.js"/>
<@js src="resource/js/components/range_datepicker.js"/>
<@js src="resource/js/vendor/foundation-datepicker.min.js"/>
<@js src="resource/js/pages/advanced_search.js"/>
<@js src="resource/js/pages/search_results_ajax.js"/>
<@js src="resource/js/components/search_results_alm.js"/>
<@js src="resource/js/components/toggle.js"/>
<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/vendor/underscore-min.js"/>

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>
<@themeConfig map="journal" value="journalName" ; v>
  <#assign journalName = v />
</@themeConfig>

<#if !isNewSearch??>
  <#assign isNewSearch = false />
</#if>

<#if isNewSearch || (searchResults.numFound == 0)>
  <#assign advancedOpen = true />
</#if>

<#if RequestParameters.q??>
  <#assign query = RequestParameters.q?html />
  <#assign advancedSearch = false />
<#elseif RequestParameters.unformattedQuery??>
  <#assign query = RequestParameters.unformattedQuery?html />
  <#assign advancedSearch = true />
<#else>
  <#assign query = otherQuery />
  <#assign advancedSearch = true />
</#if>
<#assign advancedSearchParams = {"unformattedQuery": query} />
<#if RequestParameters.filterJournals??>
  <#assign advancedSearchParams = advancedSearchParams + {"filterJournals" : RequestParameters.filterJournals} />
<#else>
</#if>

<#include "suppressSearchFilter.ftl" />

<body class="static ${journalStyle} search-results-body">
<#include "searchTemplates.ftl" />
<#include "../common/paginationTemplate.ftl" />

<#assign headerOmitMain = true />
<#include "../common/header/headerContainer.ftl" />
<form name="searchControlBarForm" id="searchControlBarForm" action="<@siteLink handlerName='simpleSearch'/>"
      method="get" <#if advancedOpen??> data-advanced-search="open"  </#if>
>
<#include "searchInputBar.ftl" />
</form>

<form name="searchResultsForm" id="searchResultsForm" action="<@siteLink handlerName='simpleSearch'/>"
      method="get">
  <section class="search-results-header">
    <div class="results-number">

    </div>
  <#-- TODO: fix this select dropdown.  See comments in the .scss.  -->
    <div class="search-sort">

      <span>Sort By:</span>

      <div class="search-results-select">
        <label for="sortOrder">
          <select name="sortOrder" id="sortOrder">
            <option value="RELEVANCE" selected="selected">Relevance</option>
            <option value="DATE_NEWEST_FIRST">Date, newest first</option>
            <option value="DATE_OLDEST_FIRST">Date, oldest first</option>
            <option value="MOST_VIEWS_30_DAYS">Most views, last 30 days</option>
            <option value="MOST_VIEWS_ALL_TIME">Most views, all time</option>
            <option value="MOST_CITED">Most cited, all time</option>
            <option value="MOST_BOOKMARKED">Most bookmarked</option>
            <option value="MOST_SHARED">Most shared in social media</option>
          </select>
        </label>
      </div>

    </div>
    <div class="search-actions">
    <#include "../macro/userSession.ftl" />
      <div class="search-alert open-search-alert-modal" data-js-tooltip-hover="trigger">
        Search Alert
        <div class="search-alert-tooltip" data-js-tooltip-hover="target">
        <#if isUserLoggedIn()>
          Save this search alert
        <#else>
          Sign in to save this search alert
        </#if>
        </div>
      </div>
      <a href="<@siteLink handlerName="searchFeed" queryParameters=parameterMap pathVariables={'feedType': 'atom'}/>"
         class="search-feed"></a>
    </div>

  </section>

  <div class="header-filter">

  </div>
</form>

<#--PG-shoudl this be a header?-->
<section class="results-container">

  <aside class="search-filters" id="searchFilters">

  </aside>
  <article class="searchResults">
    <h2>Please enter your search term above.</h2>
  </article>


</section>

<#include "../common/footer/footer.ftl" />

<div id="search-alert-modal" class="inlinePopup <#if isUserLoggedIn()>loggedIn</#if> ajax" data-reveal>
  <#if isUserLoggedIn()>
    <#include "savedSearchPopupAjax.ftl"/>
  <#else>
    <#include "loginSavedSearchPopupAjax.ftl"/>
  </#if>
  <a class="close-reveal-modal" aria-label="Close"></a>
</div>
<@renderJs />
</body>

</html>
