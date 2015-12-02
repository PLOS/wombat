
<#macro searchFilterList filterTypeName searchFilter numVisibleFilters=5>
<div>
    <h3>${filterTypeName}</h3>

    <ul class="active-filters" id="activeFilters-${filterTypeName}"><#list  searchFilter.activeFilterItems as activeFilterItem>
    <li>
      <@siteLink handlerName="simpleSearch"
      queryParameters=activeFilterItem.filteredResultsParameters ; href>
          <a href="${href}"
             data-filter-param="${activeFilterItem.filterParamName}"
             data-filter-value="${activeFilterItem.filterValue}">
              <input type="checkbox" checked/> <span> ${activeFilterItem.getDisplayName()} </span>
          </a>
      </li>
      </@siteLink>
    </#list></ul>

    <ul id="searchFilterBy${filterTypeName}">
      <#assign numFilters = 0 />

      <#list searchFilter.inactiveFilterItems as searchFilterItem>
        <#if !suppressSearchFilter(searchFilterItem)>
          <#assign numFilters = numFilters + 1 />
            <li <#if numFilters gt numVisibleFilters>data-js-toggle="toggle_target" data-visibility="none"</#if>>
              <@siteLink handlerName="simpleSearch"
              queryParameters=searchFilterItem.filteredResultsParameters ; href>
                  <a href="${href}"
                     data-filter-param="${searchFilterItem.filterParamName}"
                     data-filter-value="${searchFilterItem.filterValue}">
                      <input type="checkbox"/> <span>${searchFilterItem.displayName} (${searchFilterItem.numberOfHits})</span>
                  </a>
              </@siteLink>
            </li>
        </#if>
      </#list>
      <#if numFilters gt numVisibleFilters>
          <li class="toggle-trigger" data-js-toggle="toggle_trigger">
              <a>show more</a>
          </li>
          <li class="toggle-trigger" data-js-toggle="toggle_trigger" data-visibility="none">
              <a>show less</a>
          </li>
      </#if>
    </ul>
</div>
</#macro>

<#if searchResults.numFound != 0>
  <#if searchFilters?? >
  <aside class="search-filters" id="searchFilters">
    <#if searchFilters.journal??>
      <@searchFilterList "Journal", searchFilters.journal/>
    </#if>
    <#if searchFilters.subject_area??>
      <@searchFilterList "Subject Area", searchFilters.subject_area/>
    </#if>
    <#if searchFilters.article_type??>
      <@searchFilterList "Article Type", searchFilters.article_type/>
    </#if>
    <#if searchFilters.author??>
      <@searchFilterList "Author", searchFilters.author/>
    </#if>
    <#if searchFilters.section??>
      <@searchFilterList "Where my keywords appear", searchFilters.section/>
    </#if>
      <div>
          <form class="date-filter-form" name="dateFilterForm" id="dateFilterForm" action="<@siteLink path='search'/>"
                method="get">
              <h3>Publication Date</h3>

              <input name="filterStartDate" id="dateFilterStartDate" type="text" class="datepicker"
                     <#if filterStartDate??>value="${filterStartDate}"</#if>>

              <div>&nbsp;to</div>
              <input name="filterEndDate" id="dateFilterEndDate" type="text" class="datepicker"
                     <#if filterEndDate??>value="${filterEndDate}"</#if>>

              <input type="submit" id="dateFilterSubmitButton" value="Apply">
            <#list parameterMap?keys as param>
              <#if param != 'filterStartDate' && param != 'filterEndDate'>
                  <input type="hidden" name="${param}" value="${parameterMap[param][0]}"/>
              </#if>
            </#list>
          </form>
      </div>
  </aside>
  </#if>
</#if>
