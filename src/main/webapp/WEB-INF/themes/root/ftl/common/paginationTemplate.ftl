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