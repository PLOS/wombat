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

<#include "edboardStaticBody.ftl" />
<#include "paginationTemplate.ftl" />

<script type="text/javascript">
  <#-- Note, solrHost value will will be modified during a wombat salt highstate. -->
  <#-- (See GIT repo file PLOS-Formulas/wombat-formula/dev/wombat/init.sls#themes_eboard_search_host). -->
  var SOLR_CONFIG = SOLR_CONFIG || {};
  SOLR_CONFIG.solrHost = "https://api.plos.org/search";
</script>

<div class="editors-search-filter">
  <label for="editors-search-input">Search for:</label>
  <input type="text" placeholder="People, Areas of Expertise,..." id="editors-search-input"/>
  <button class="editors-search-button">Search</button>
  <button class="editors-search-reset"></button>
  <span id="loading" class="icon-spinner"></span>
</div>

<script type="text/template" id="autocompleteSectionTitleTemplate">
  <h5><%= sectionTitle %></h5>
</script>
<script type="text/template" id="autocompleteItemTemplate">
  <p><%= name %></p>
</script>

<script type="text/template" id="editorsListTemplate">
  <ul class="editors-list">
    <% _.each(docs, function (item) { %>
      <li class="item">
        <% if (item.doc_type == "section_editor") { %><i class="section-editor-icon"></i><br><% } %>
        <strong><%= item.ae_name %></strong><% if (item.doc_type == "section_editor") { %> Section Editor<% } %><br>
        <% if (!_.isEmpty(item.ae_ORCID)) { %><a href="https://orcid.org/<%= item.ae_ORCID %>" target="_blank"><img src="<@siteLink handlerName="staticResource" wildcardValues=["img/orcid-16x16.png"]/>" /> orcid.org/<%= item.ae_ORCID %></a><br><% } %>

        <% if (_.isArray(item.ae_institute)) { %><%= item.ae_institute.join(', ') %><% } else { %><%= item.ae_institute %><% } %><br>
          <% if (_.isArray(item.ae_country)) { %><%= item.ae_country.join(', ') %><% } else { %><%= item.ae_country %><% } %> <br>
        Subject Areas: <% if (_.isArray(item.ae_subject)) { %><%= item.ae_subject.join(', ') %><% } else { %><%= item.ae_subject %><% } %>
      </li>
    <% }); %>
  </ul>
</script>

<script type="text/template" id="resultsCounterTemplate">
  <p class="counters">
    <% if(totalRecords > 0) { %>
    Displaying <%=firstRecord%>-<%=lastRecord%> of <%=totalRecords%> Editors<% if(!_.isEmpty(currentSearchTerm)) { %> for <strong>'<%=currentSearchTerm%>'</strong><% } %>.
    <% } else { %>
    No result found <% if(!_.isEmpty(currentSearchTerm)) { %> for <strong>'<%=currentSearchTerm%>'</strong><% } %>.
    <% } %>
  </p>
</script>

<div class="results">

</div>