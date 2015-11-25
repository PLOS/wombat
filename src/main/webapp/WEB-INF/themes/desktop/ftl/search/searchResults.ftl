<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign depth = 0 />
<#assign title = "Search Results" />
<#assign cssFile="search-results.css"/>
<@cssLink target="resource/css/jquery-ui.min.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/legacyLink.ftl" />

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/pages/search_results.js"/>
<@js src="resource/js/components/toggle.js"/>
<@js src="resource/js/vendor/jquery-ui.min.js"/>

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>
<@themeConfig map="journal" value="journalName" ; v>
  <#assign journalName = v />
</@themeConfig>

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
<#assign advancedSearchLink = "${legacyUrlPrefix}search/advanced?filterJournals=${journalKey}&unformattedQuery=${query}&noSearchFlag=set" />

<#include "suppressSearchFilter.ftl" />

<body class="static ${journalStyle} search-results-body">

<#assign headerOmitMain = true />
<#include "../common/header/headerContainer.ftl" />
<form name="searchControlBarForm" id="searchControlBarForm" action="<@siteLink path='search'/>" method="get">
    <div class="search-results-controls">

        <div class="search-results-controls-first-row">
            <fieldset class="search-field">
                <legend>Search</legend>
                <label for="controlBarSearch">Search</label>
                <input id="controlBarSearch" type="text" name="${advancedSearch?string('unformattedQuery', 'q')}"
                       value="${query}" required/>
                <button id="searchFieldButton" type="submit"><span class="search-icon"></span></button>
            </fieldset>
            <a id="advancedSearchLink" class="search-results-advanced-search-submit" href="${advancedSearchLink}">Advanced
                Search</a>
        </div>


    </div>
<#if searchResults.numFound == 0>
    <section class="search-results-none-found">
        <p>You searched for articles that have all of the following:</p>

        <p>Search Term: "<span>${query}</span>"</p>

        <p>Journal: "<span>${journalName}</span>"</p>

        <p>
            There were no results; please
            <a href="${legacyUrlPrefix}search/advanced?filterJournals=${journalKey}&unformattedQuery=${query}&noSearchFlag=set">refine
                your search</a>
            and try again.</p>
    </section>
</#if>
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
            <div class="search-alert" data-js-tooltip-hover="trigger">
                Search Alert
                <div class="search-alert-tooltip" data-js-tooltip-hover="target">
                    This feature temporarily unavailable.
                </div>
            </div>
            <div class="search-feed" data-js-tooltip-hover="trigger">
                <div class="search-feed-tooltip" data-js-tooltip-hover="target">
                    This feature temporarily unavailable.
                </div>
            </div>
        </div>

    </section>
</#if>

</form>

<#if searchResults.numFound != 0>
<div class="filter-view-container">
    <section class="filter-view">
      <#if (isFiltered)>
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
      </#if>
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

<#--PG-shoudl this be a header?-->

<section class="results-container">

  <#include "searchFilters.ftl" />
    <article>
    <#if searchResults.numFound != 0>

        <dl id="searchResultsList" class="search-results-list">
          <#list searchResults.docs as doc>
              <dt data-doi="${doc.id}" class="search-results-title">
                  <a href="${doc.link}">${doc.title}</a>
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

                  <p class="search-results-doi">${doc.id}</p>
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
                  <div class="search-results-alm-container">
                      <p class="search-results-alm-loading">
                          Loading metrics information...
                      </p>

                      <p class="search-results-alm" data-doi="${doc.id}">
                          <a href="${legacyUrlPrefix}article/metrics/info:doi/${doc.id}#viewedHeader">Views: </a> •
                          <a href="${legacyUrlPrefix}article/metrics/info:doi/${doc.id}#citedHeader">Citations: </a> •
                          <a href="${legacyUrlPrefix}article/metrics/info:doi/${doc.id}#savedHeader">Saves: </a> •
                          <a href="${legacyUrlPrefix}article/metrics/info:doi/${doc.id}#discussedHeader">Shares: </a>
                      </p>

                      <p class="search-results-alm-error">
                <span class="fa-stack icon-warning-stack">
                  <i class="fa fa-exclamation fa-stack-1x icon-b"></i>
                  <i class="fa icon-warning fa-stack-1x icon-a"></i>
                </span>Metrics unavailable. Please check back later.
                      </p>
                  </div>
              </dd>
          </#list>
        </dl>
    </#if>

    <#assign numPages = (searchResults.numFound / resultsPerPage)?ceiling />
    <#assign currentPage = (RequestParameters.page!1)?number />
    <#assign path = "search" />
    <#assign alwaysShow = true />
    <#include "../common/paging.ftl" />

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
<#include "../common/footer/footer.ftl" />
<@renderJs />
</body>
</html>
