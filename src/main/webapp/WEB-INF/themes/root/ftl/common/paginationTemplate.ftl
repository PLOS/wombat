<script type="text/template" id="resultsPaginationTemplate">
  <nav id="article-pagination" class="nav-pagination"
       data-pagination-total-records="<%= totalRecords %>"
       data-pagination-current-page="<%= currentPage %>"
       data-pagination-items-per-page="<%= itemsPerPage %>">
    <% _.each(pages, function (item) {%>
    <% if(item.type == 'previous-page') { %>
    <<% if(item.disabled) { %>span<% } else { %>a<% } %> id="prevPageLink" href="#" class="previous-page switch<% if(item.disabled) { %> disabled<% } %>">
    <span class="icon"></span>
    <span class="icon-text">Previous Page</span>
  </<% if(item.disabled) { %>span<% } else { %>a<% } %>>
  <% } else if(item.type == 'number') { %>
  <a class="number seq<% if(item.current) { %> active<% } %> text-color" data-page="<%= item.page %>"><%= item.page %></a>
  <% } else if(item.type == 'divider') { %>
  <span class="skip">...</span>
  <% } else if(item.type == 'next-page') { %>
  <<% if(item.disabled) { %>span<% } else { %>a<% } %> id="nextPageLink" href="#" class="next-page switch <% if(item.disabled) { %> disabled<% } %>">
  <span class="icon"></span>
  <span class="icon-text">Next Page</span>
  </<% if(item.disabled) { %>span<% } else { %>a<% } %>>
  <% } %>
  <% }); %>
  </nav>
</script>