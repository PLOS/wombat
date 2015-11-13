<#macro searchFilter filterTypeName searchFilter>
<div>
    <h3>${filterTypeName}</h3>
    <ul id="id="activeFilters-${filterTypeName}""
  <#list  searchFilter.activeFilterItems as activeFilterItem>
 <li> ${activeFilterItem.getDisplayName()}
    <@siteLink handlerName="simpleSearch"
    queryParameters=activeFilterItem.filteredResultsParameters ; href>
        <a href="${href}"
           data-filter-param="${activeFilterItem.filterParamName}"
           data-filter-value="${activeFilterItem.filterValue}">
            x
        </a>
    </li>
    </@siteLink>
  </#list>
    <hr/>
    <ul id="searchFilterBy${filterTypeName}">
      <#list  searchFilter.searchFilterResult as searchFilterItem>
        <#if !suppressSearchFilter(searchFilterItem) >
            <li <#if searchFilterItem_index gt 5>data-js-toggle="toggle_target" data-visibility= "none"</#if>>
             <@siteLink handlerName="simpleSearch"
              queryParameters=searchFilterItem.filteredResultsParameters ; href>
                  <a href="${href}"
                     data-filter-param="${searchFilterItem.filterParamName}"
                     data-filter-value="${searchFilterItem.filterValue}">
                      <input type="checkbox" />  ${searchFilterItem.displayName} (${searchFilterItem.numberOfHits})
                  </a>
              </@siteLink>
            </li>
        </#if>
      </#list>
      <#if searchFilter.searchFilterResult?size gt 5>
          <li class="toggle-trigger" data-js-toggle="toggle_trigger">
              <a>show more</a>
          </li>
          <li class="toggle-trigger" data-js-toggle="toggle_trigger"  data-visibility= "none">
              <a>show less</a>
          </li>
      </#if>
    </ul>
</div>
</#macro>

<#if searchResults.numFound != 0>
  <#if searchFilters?? >
  <aside id="searchFilters">
    <#if searchFilters.journal??>
      <@searchFilter "Journal", searchFilters.journal/>
    </#if>
    <#if searchFilters.subject_area??>
      <@searchFilter "Subject Area", searchFilters.subject_area/>
    </#if>
    <#if searchFilters.article_type??>
      <@searchFilter "Article Type", searchFilters.article_type/>
    </#if>
    <#if searchFilters.author??>
      <@searchFilter "Author", searchFilters.author/>
    </#if>
    <#if searchFilters.section??>
      <@searchFilter "Where my keywords appear", searchFilters.section/>
    </#if>
      <div>
          <form name="dateFilterForm" id="dateFilterForm" action="<@siteLink path='search'/>" method="get">
              <h3>Date</h3>
              <div>Published between</div>
              <input name="filterStartDate" id="dateFilterStartDate" type="text" class="datepicker"
                     <#if filterStartDate??>value="${filterStartDate}"</#if>>
              <div>to</div>
              <input name="filterEndDate" id="dateFilterEndDate" type="text" class="datepicker"
                     <#if filterEndDate??>value="${filterEndDate}"</#if>>
              <input type="submit" id="dateFilterSubmitButton" value="Apply">
            <#list parameterMap?keys as param>
              <#if param != 'filterStartDate' && param != 'filterEndDate'>
                  <input type="hidden" name="${param}" value="${parameterMap[param][0]}" />
              </#if>
            </#list>
          </form>
      </div>
  </aside>
  </#if>
</#if>
