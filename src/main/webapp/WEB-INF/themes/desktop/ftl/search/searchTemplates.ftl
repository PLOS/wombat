<script id="searchListItemTemplate" type="text/template">
  <dl id="searchResultsList" class="search-results-list">
    <% _.each(results, function(item, index) { %>
    <dt data-doi="<%= item.id %>" class="search-results-title">
      <a href="<%= item.link %>"><% if (!_.isEmpty(item.title_display)) { print(item.title_display); } else {
        print(item.title); } %></a>
    </dt>
    <dd id="article-result-<%= index %>">
      <% if(!_.isEmpty(item.author_display)) { %>
      <p class="search-results-authors">
        <% print(item.author_display.join(', ')); %>
      </p>
      <% } %>

      <p>
        <% if(!_.isEmpty(item.article_type)) { %>
        <span id="article-result-<%= index %>-type"><%= item.article_type %></span> |
        <% } %>
                      <span id="article-result-<%= index %>-date">
                published <%= moment(item.publication_date).format("DD MMM YYYY") %>
                          |
              </span>
        <% if(!_.isEmpty(item.cross_published_journal_name)) { %>
                        <span id="article-result-<%= index %>-journal-name">
                        <%= item.cross_published_journal_name[0] %>
                        </span>
        <% } %>
      </p>

      <p class="search-results-doi"><a href="http://dx.doi.org/<%= item.id %>">http://dx.doi.org/<%= item.id %></a>
      </p>
      <div class="search-results-alm-container" data-doi="<%= item.id %>" data-index="<%= index %>">

        <p class="search-results-alm-loading">
          Loading metrics information...
        </p>

      </div>
    </dd>
    <% }); %>
</script>

<script id="search-results-alm" type="text/template">

  <p style="display: block;" class="search-results-alm" data-doi="">
    <a href="/wombat/DesktopPlosOne/article/metrics?id=10.1371%2Fjournal.pone.0009020#viewedHeader">Views: <%= viewCount
      %></a>
    •
    <a href="/wombat/DesktopPlosOne/article/metrics?id=10.1371%2Fjournal.pone.0009020#citedHeader">Citations: <%=
      citationCount %></a>
    •
    <a href="/wombat/DesktopPlosOne/article/metrics?id=10.1371%2Fjournal.pone.0009020#savedHeader">Saves: <%= saveCount
      %></a>
    •
    Shares: <%= shareCount %>
  </p>

</script>
<script id="search-results-alm-error" type="text/template">
  <p class="search-results-alm-error">
    <span class="fa-stack icon-warning-stack">
      <i class="fa fa-exclamation fa-stack-1x icon-b"></i>
      <i class="fa icon-warning fa-stack-1x icon-a"></i>
    </span>Metrics unavailable. Please check back later.
  </p>
</script>


<script id="searchListFilterSectionTemplate" type="text/template">
  <div class="filterSection">
    <h3><%= filterTitle %></h3>
    <% if (activeFilterItems.length > 0) { %>
    <ul class="active-filters">
      <% _.each(activeFilterItems, function(item) { %>
      <li>
        <a href="#" data-filter-param-name="<%= item.filterParamName %>" data-filter-value="<%= item.filterValue %>">
          <input checked="" type="checkbox"> <span> <%= item.displayName %> </span>
        </a>
      </li>
      <% }); %>
    </ul>
    <% } %>

    <ul>
      <% _.each(inactiveFilterItems, function(item) { %>
      <li>
        <a href="#" data-filter-param-name="<%= item.filterParamName %>" data-filter-value="<%= item.filterValue %>">
          <input type="checkbox" data-filter-param-name="<%= item.filterParamName %>"
                 data-filter-value="<%= item.filterValue %>">
          <span><%= item.displayName %>  (<%= item.numberOfHits %>)</span>
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
  </div>
</script>

<script type="text/template" id="searchHeaderFilterTemplate">
  <% if (activeFilterItems.length > 0) { %>
  <div class="filter-view-container">
    <section class="filter-view">
      <h3 class="filter-label">Filters:</h3>
      <div class="filter-block">
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
  <div class="search-results-num-per-page">
    Results per page
    <div class="search-results-num-per-page-selector">
      <label for="paging">
        <select name="resultsPerPageDropdown" id="resultsPerPageDropdown">
          <% _.each(pages, function(active, quantity) { %>
          <option value="<%= quantity %>"
          <% if (active) { %>selected="selected"<% } %>><%= quantity %></option>
          <% }); %>
        </select>

      </label>
    </div>
  </div>
</script>

<script type="text/template" id="searchNoResultsTemplate">
  <p>You searched for articles that have all of the following:</p>

  <p>Search Term: "<span><%= q %></span>"</p>

  <p>There were no results; please refine your search above and try again.</p>
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