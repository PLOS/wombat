<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<script id="searchListItemTemplate" type="text/template">
  <dl id="searchResultsList" class="search-results-list">
    <% _.each(results, function(item, index) { %>
    <dt data-doi="<%= item.id %>" class="search-results-title">

       <p id="article-result-<%= index %>-date" class="date">
                Posted: <%= moment.utc(item.publication_date).format("MMMM D, YYYY") %>
              </p>
      <p><a href="<%= item.link %>"><% if (!_.isEmpty(item.title_display)) { print(item.title_display); } else {
        print(item.title); } %></a></p>
    </dt>

    <dd id="article-result-<%= index %>">
      <% if(!_.isEmpty(item.author_display)) { %>
      <p class="search-results-authors">
        <% print(item.author_display.join(' - ')); %>
      </p>
      <% } %>
      <p class="search-results-doi">DOI: <a href="https://doi.org/<%= item.id %>">https://doi.org/<%= item.id %></a>
      </p>
    </dd>
    <% }); %>
</script>




<script id="searchListFilterSectionTemplate" type="text/template">
  <div class="filterSection">
    <h3><%= filterTitle %></h3>
    <% if (activeFilterItems.length > 0) { %>
    <ul class="active-filters" id="active-<%= activeFilterItems[0].filterParamName %>">
      <% _.each(activeFilterItems, function(item) { %>
      <li>
        <a href="#" data-filter-param-name="<%= item.filterParamName %>" data-filter-value="<%= item.filterValue %>">
          <input checked="" type="checkbox"> <span> <%= item.displayName %> </span>
        </a>
      </li>
      <% }); %>
    </ul>
    <% } %>
    <% if (inactiveFilterItems.length > 0) { %>
    <ul class="inactive-filters" id="inactive-<%= inactiveFilterItems[0].filterParamName %>">
      <% _.each(inactiveFilterItems, function(item) { %>
      <li>
        <a href="#" data-filter-param-name="<%= item.filterParamName %>" data-filter-value="<%= item.filterValue %>">
          <input type="checkbox" data-filter-param-name="<%= item.filterParamName %>"
                 data-filter-value="<%= item.filterValue %>">
          <span><%= item.displayName %>  (<%= s.numberFormat(item.numberOfHits, 0) %>)</span>
        </a>
      </li>
      <% }); %>
      <li class="toggle-trigger" data-show-more-items="true">
        <a>show more</a>
      </li>
      <li class="toggle-trigger" data-show-less-items="true">
        <a>show less</a>
      </li>
    </ul>
    <% } %>

  </div>

</script>

<script type="text/template" id="searchListFilterDateTemplate">
  <form class="date-filter-form">
    <h3>Publication Date</h3>
    <input name="filterStartDate" id="dateFilterStartDate" required type="text"
    <% if(!_.isEmpty(start)) { print('value="'+start+'"'); } %> class="fdatepicker">
    <div>&nbsp;to</div>
    <input name="filterEndDate" id="dateFilterEndDate" required type="text"
    <% if(!_.isEmpty(end)) { print('value="'+end+'"'); } %> class="fdatepicker">
    <button type="submit">Apply</button>
  </form>
</script>

<script type="text/template" id="searchHeaderFilterTemplate">
  <% if (activeFilterItems.length > 0 || searchDateFilters.start != null && searchDateFilters.end != null) { %>
  <div class="filter-view-container">
    <section class="filter-view">
      <h3 class="filter-label">Filters:</h3>
      <div class="filter-block">
        <% if(searchDateFilters.start != null && searchDateFilters.end != null) { %>
        <div class="filter-item" id="filter-date">
          <%= moment(searchDateFilters.start).format('ll') %> TO <%= moment(searchDateFilters.end).format('ll') %>
          <a href="#" data-filter-param-name="filterDates" data-filter-value="date">
            &nbsp;
          </a>
        </div>
        <% } %>
        <% _.each(activeFilterItems, function(item) { %>
        <div class="filter-item">
          <%= item.displayName %>
          <a href="#" data-filter-param-name="<%= item.filterParamName %>" data-filter-value="<%= item.filterValue %>">
            &nbsp;
          </a>
        </div>
        <% }); %>
      </div>
      <div class="clear-filters">
        <a id="clearAllFiltersButton" href="#">Clear all filters</a>
      </div>
    </section>
  </div>
  <% } %>
</script>

<script type="text/template" id="searchPagingSelectorTemplate">

</script>



<script type="text/template" id="searchresultNumber">


</script>

<script type="text/template" id="searchNoResultsTemplate">
  <div class="search-results-none-found">
    <p>You searched for articles that have all of the following:</p>

    <p>Search Term: "<span><%= q %></span>"</p>

    <p>There were no results; please refine your search above and try again.</p>
  </div>
</script>

<script type="text/template" id="searchResultsNumberTemplate">
    <div class="results"><%= resultsCount %> items</div>

    <div class="query"> "<span><%= q %></span>"</div>

</script>

<script type="text/template" id="searchParseErrorTemplate">
  <h2>
    Your query is invalid and may contain one of the following invalid characters: " [ ] { } \ /
    <br/>
    Please try a new search above.
  </h2>
</script>
<script type="text/template" id="searchGeneralErrorTemplate">
  <h2>
    There was a problem loading search results. Please edit your query or try again later.
  </h2>
</script>