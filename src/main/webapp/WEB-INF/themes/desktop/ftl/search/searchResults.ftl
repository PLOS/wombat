<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign title = "Search Results" />
<#assign cssFile="search-results.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../macro/searchResultsAlm.ftl" />

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/components/range_datepicker.js"/>
<@js src="resource/js/vendor/foundation-datepicker.min.js"/>
<@js src="resource/js/pages/advanced_search.js"/>
<@js src="resource/js/pages/search_results.js"/>
<@js src="resource/js/components/search_results_alm.js"/>
<@js src="resource/js/components/toggle.js"/>
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

<#assign headerOmitMain = true />
<#include "../common/header/headerContainer.ftl" />
<form name="searchControlBarForm" id="searchControlBarForm" action="<@siteLink handlerName='simpleSearch'/>" method="get" <#if advancedOpen??> data-advanced-search="open"  </#if>
>
<#include "searchInputBar.ftl" />
</form>

<#if !isNewSearch>
<form name="searchResultsForm" id="searchResultsForm" action="<@siteLink handlerName='simpleSearch'/>"
      method="get">
  <input type="hidden" pattern=".{1,}" name="${advancedSearch?string('unformattedQuery',
  'q')}" value="${query}" required/>
  <#if searchResults.numFound != 0>
    <section class="search-results-header">
      <div class="results-number">

      ${searchResults.numFound}
        <#if searchResults.numFound == 1>
          result
        <#else>
          results
        </#if>
        <#if query?? && query?length gt 0>
          for <strong>${query}</strong>
        </#if>

      </div>
    <#-- TODO: fix this select dropdown.  See comments in the .scss.  -->
      <div class="search-sort">

        <span>Sort By:</span>

        <div class="search-results-select">
          <label for="sortOrder">
            <select name="sortOrder" id="sortOrder">
              <#list sortOrders as sortOrder>
                <option value="${sortOrder}" <#if (selectedSortOrder == sortOrder)>
                        selected="selected"</#if>>${sortOrder.description}</option>
              </#list>
            </select>
          </label>
        </div>

      </div>
      <div class="search-actions">
        <#include "../macro/userSession.ftl" />
        <div class="search-alert" data-js-tooltip-hover="trigger"
          <#if isUserLoggedIn()>
             id="save-search-link"
          <#else>
             id="login-link"
          </#if> >
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
  </#if>

  <#if isFiltered>
    <div class="filter-view-container">
      <section class="filter-view">
        <h3 class="filter-label">Filters:</h3>
        <div class="filter-block">
          <#if (filterStartDate??)>
            <div class="filter-item" id="filter-date">
            ${filterStartDate?date("yyyy-MM-dd")?string} TO ${filterEndDate?date("yyyy-MM-dd")?string}
              <@siteLink handlerName="simpleSearch" queryParameters=dateClearParams ; href>
                <a href="${href}">&nbsp;</a>
              </@siteLink>
            </div>
            <input type="hidden" name="filterStartDate" value="${filterStartDate}" placeholder="YYYY-MM-DD"/>
            <#if (filterEndDate??)>
              <input type="hidden" name="filterEndDate" value="${filterEndDate}"/>
            </#if>
          </#if>
          <#if (activeFilterItems?size > 0)>
            <#list activeFilterItems as item>
              <div class="filter-item">
              ${item.getDisplayName()}
                <@siteLink handlerName="simpleSearch"
                queryParameters=item.filteredResultsParameters ; href>
                  <a href="${href}"
                     data-filter-param="${item.filterParamName}"
                     data-filter-value="${item.filterValue}">&nbsp;
                  </a>
                </@siteLink>
                <input type="hidden" name="${item.filterParamName}" value="${item.filterValue}" />
              </div>
            </#list>
          </#if>
        </div>
        <div class="clear-filters">
          <@siteLink handlerName="simpleSearch" queryParameters=clearAllFilterParams ; href>
            <a id="clearAllFiltersButton" href="${href}">Clear all filters</a>
          </@siteLink>

          <input type="hidden" name="resultsPerPage" id="resultsPerPage" value="${resultsPerPage}"/>
          <#if RequestParameters.page??>
            <input type="hidden" name="page" value="${RequestParameters.page}"/>
          </#if>
        </div>
      </section>
    </div>
  </#if>
</form>
</#if>
<#if isNewSearch>
<section class="search-results-none-found">
  <p>
    <#if cannotParseQueryError??>
      Your query is invalid and may contain one of the following invalid characters: " [ ] { } \ /
      <br/>
      Please try a new search above.
    <#elseif unknownQueryError??>
      There was a problem loading search results. Please edit your query or try again later.
    <#else>
      Please enter your search term above.
    </#if>
  </p>
</section>
<#elseif searchResults.numFound == 0>

<section class="search-results-none-found">
  <p>You searched for articles that have all of the following:</p>

  <p>Search Term: "<span>${query}</span>"</p>

  <p>There were no results; please refine your search above and try again.</p>
</section>
</#if>

<#--PG-shoudl this be a header?-->
<#if !isNewSearch>
<section class="results-container">

  <#include "searchFilters.ftl" />
  <article>
    <#if searchResults.numFound != 0>

      <dl id="searchResultsList" class="search-results-list">
        <#list searchResults.docs as doc>
          <#if doc.title_display?? >
            <#assign docTitle=doc.title_display />
          <#else>
            <#assign docTitle=doc.title />
          </#if>
          <dt data-doi="${doc.id}" class="search-results-title">
            <a href="${doc.link}">${docTitle}</a>
          </dt>
          <dd id="article-result-${doc_index}">
            <p class="search-results-authors">
              <#list doc.author_display![] as author>
              ${author}<#if author_has_next>,</#if>
              </#list>
            </p>

            <p>
              <#if doc.article_type??>
                <span id="article-result-${doc_index}-type">${doc.article_type}</span> |
              </#if>
              <span id="article-result-${doc_index}-date">
                published <@formatJsonDate date="${doc.publication_date}" format="dd MMM yyyy" interpretDateAsLocalTime="true" />
                          |
              </span>
              <#if doc.cross_published_journal_name??>
                <span id="article-result-${doc_index}-journal-name">
                ${doc.cross_published_journal_name[0]}
                        </span>
              </#if>
            </p>

            <#include "../macro/doiAsLink.ftl" />
            <p class="search-results-doi"><@doiAsLink doc.id/></p>
            <#if (doc.retraction?? && doc.retraction?length gt 0) || doc.expression_of_concern!?size gt 0>
              <div class="search-results-eoc">
                <span></span>
                <#if doc.retraction?length gt 0>
                  <a href="article?id=${doc.retraction}">This article has been retracted.</a>
                <#else>
                  <a href="article?id=${doc.id}">View Expression of Concern</a>
                </#if>
              </div>
            </#if>
            <@searchResultsAlm doc.id doc.journalKey />
          </dd>
        </#list>
      </dl>
    </#if>

    <#assign numPages = (searchResults.numFound / resultsPerPage)?ceiling />
    <#assign currentPage = (RequestParameters.page!1)?number />
    <#assign path = "search" />
    <#include "../common/paging.ftl" />
    <@paging numPages currentPage path parameterMap true />

    <#if numPages gt 1>
      <div class="search-results-num-per-page">
        Results per page
        <div class="search-results-num-per-page-selector">
          <label for="paging">
            <select name="resultsPerPageDropdown" id="resultsPerPageDropdown">
              <option value="15" <#if (selectedResultsPerPage == 15)>selected="selected"</#if>>15</option>
              <option value="30" <#if (selectedResultsPerPage == 30)>selected="selected"</#if>>30</option>
              <option value="60" <#if (selectedResultsPerPage == 60)>selected="selected"</#if>>60</option>
            </select>
          </label>
        </div>
      </div>
    </#if>

  </article>


</section>
</#if>
<#include "../common/footer/footer.ftl" />
<#if alertQuery??>
<div id="save-search-box" class="inlinePopup">
  <#include "savedSearchPopup.ftl"/>
</div>

<div id="login-box" class="login inlinePopup">
  <#include "loginSavedSearchPopup.ftl"/>
</div>
</#if>
<@renderJs />
</body>
</html>
