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

<script type="text/template" id="metricsTileTemplate">
    <div id="<%=  source_name %>OnArticleMetricsTab" class="metrics_tile">
        <a href="<%=  url %>"><img id="<%=  source_name %>ImageOnArticleMetricsTab" src="<%=  imgSrc %>"
                                   alt="<%=  linkText %> <%=  name %>" class="metrics_tile_image"/></a>
        <div class="metrics_tile_footer" onclick="location.href=<%=  url %>">
            <a href="<%=  url %>"><%= linkText %></a></div>
    </div>
</script>

<script type="text/template" id="metricsTileTemplateNoLink">
    <div id="<%= source_name %>OnArticleMetricsTab" class="metrics_tile_no_link">
        <img id="<%= source_name %>ImageOnArticleMetricsTab" src="<%= imgSrc %>" alt="<%= linkText %> <%= name %>"
             class="metrics_tile_image"/>
        <div class="metrics_tile_footer_no_link"><%= linkText %></div>
    </div>
</script>

<script type="text/template" id="metricsTileFacebookTooltipTemplate">
    <div class="tileTooltipContainer">
        <table class="tile_mini tileTooltip" data-js-tooltip-hover="target">
            <thead>
            <tr>
                <th>Likes</th>
                <th>Shares</th>
                <th>Posts</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="data1"> <%= likes.format(0, '.', ',') %></td>
                <td class="data2"> <%= shares.format(0, '.', ',') %></td>
                <td class="data1"> <%= comments.format(0, '.', ',') %></td>
            </tr>
            </tbody>
        </table>
    </div>
</script>

<script type="text/template" id="metricsTileMendeleyTooltipTemplate">
    <div class="tileTooltipContainer">
        <table class="tile_mini tileTooltip" data-js-tooltip-hover="target">
            <thead>
            <tr>
                <th>Individuals</th>
                <th>Groups</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="data1"> <%= individuals.format(0, '.', ',') %></td>
                <td class="data2"> <%= groups.format(0, '.', ',') %></td>
            </tr>
            </tbody>
        </table>
    </div>
</script>

<script type="text/template" id="metricsTileFigshareTooltipTemplate">
    <div id="dropdown-figshare" class="metric-dropdown" data-dropdown-content>
        <div class="close-button" onclick="Foundation.libs.dropdown.close(jQuery('#dropdown-figshare'));">
            <span>&nbsp;</span>
            <a class="button-container">
                <i class="fa fa-times"></i>
            </a>
        </div>
        <table class="tile_mini">
            <% _.each(items, function(item) { %>
            <tr>
                <td>
                  <% if (typeof(item.link) !== "undefined") { %>
                    <a href="<%= item.link %>" target=_blank> <%= item.title %></a>
                  <% } else { %>
                    <%= item.title %>
                  <% } %>
                </td>
                <td class="data1">
                  <% if (typeof(item.totalStat) !== "undefined") { %>
                    <%= item.totalStat %>
                  <% } %>
                </td>
            </tr>
            <% }); %>
        </table>
    </div>
</script>

<script type="text/template" id="citedSectionNoDataTemplate">
    No related citations found <br/>
    Search for citations in <a href="<%= googleLink %>">Google Scholar</a>
</script>

<script type="text/template" id="viewedSectionNoDataTemplate">
    Viewed data is not available. Please try again later.
</script>

<script type="text/template" id="viewedSectionNewArticleErrorTemplate">
    This article was only recently published. Although we update our data on a daily basis (not in real time), there may be a 48-hour delay before the most recent numbers are available.
</script>

<script type="text/template" id="viewedHighchartTooltipTemplate">
    <table id="mini" cellpadding="0" cellspacing="0">
        <tr>
            <td></td>
            <td colspan="2">Views in <%= formattedDate %></td>
            <td colspan="2">Views through <%= formattedDate %></td>
        </tr>
        <tr>
            <th>Source</th>
            <th class="header1">PLOS</th>
            <th class="header2">PMC</th>
            <th class="header1">PLOS</th>
            <th class="header2">PMC</th>
        </tr>
        <tr>
            <td>HTML</td>
            <td class="data1"> <%= source.counterViews.totalHTML %></td>
            <td class="data2"> <%= source.pmcViews.totalHTML.format(0, '.', ',') %>
            </td>
            <td class="data1"><%= source.counterViews.cumulativeHTML.format(0, '.', ',') %></td>
            <td class="data2"><%= source.pmcViews.cumulativeHTML.format(0, '.', ',') %></td>
        </tr>
        <tr>
            <td>PDF</td>
            <td class="data1"><%= source.counterViews.totalPDF %></td>
            <td class="data2"><%= source.pmcViews.totalPDF.format(0, '.', ',') %></td>
            <td class="data1"><%= source.counterViews.cumulativePDF.format(0, '.', ',') %></td>
            <td class="data2"><%= source.pmcViews.cumulativePDF.format(0, '.', ',') %></td>
        </tr>
        <tr>
            <td>XML</td>
            <td class="data1"><%= source.counterViews.totalXML %></td>
            <td class="data2">n.a.</td>
            <td class="data1"><%= source.counterViews.cumulativeXML.format(0, '.', ',') %></td>
            <td class="data2">n.a.</td>
        </tr>
        <tr>
            <td>Total</td>
            <td class="data1"><%= source.counterViews.total %></td>
            <td class="data2"><%= source.pmcViews.total.format(0, '.', ',') %></td>
            <td class="data1"><%= source.counterViews.cumulativeTotal.format(0, '.', ',') %></td>
            <td class="data2"><%= source.pmcViews.cumulativeTotal.format(0, '.', ',') %>
            </td>
        </tr>
    </table>
</script>

<script type="text/template" id="relativeMetricTemplate">
    <div id="averageViewsSummary">
        <div>
            <span class="colorbox"></span>
            &nbsp;Compare average usage for articles published in <b><%= yearPublished %></b> in the
            subject area: <a href="<%= referenceUrl %>" class="ir" title="More information">info</a>
        </div>
        <div>
            <select id="subject_areas">
                <% _.each(subjectAreasList, function(item) { %>
                <option value="<%= item.id %>"><%= item.title %></option>
                <% _.each(item.children, function(child) { %>
                <option value="<%= child.id %>">&nbsp;&nbsp;&nbsp;<%= child.title %></option>
                <% }); %>
                <% }); %>
            </select>
            &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
            <@siteLink handlerName="newAdvancedSearch" ; href>
            <a id="linkToRefset" href="" data-base-link="${href}">Show reference set</a>
            </@siteLink>
        </div>
    </div>
</script>

<script type="text/template" id="pageViewsSummary">
    <div id="pageViewsSummary">
        <div id="left">
            <div class="header">Total Article Views</div>
            <div class="totalCount"> <%= total %></div>
            <div class="pubDates"> <%= pubDatesFrom %> (publication date)
                <br>through <%= pubDatesTo %> *
            </div>
        </div>
        <div id="right">
            <table id="pageViewsTable">
                <tbody>
                <tr>
                    <th></th>
                    <th nowrap="">HTML Page Views</th>
                    <th nowrap="">PDF Downloads</th>
                    <th nowrap="">XML Downloads</th>
                    <th>Totals</th>
                </tr>
                <tr>
                    <td class="source1">PLOS</td>
                    <td> <%= totalCounterHTML %></td>
                    <td> <%= totalCounterPDF %></td>
                    <td> <%= totalCounterXML %></td>
                    <td class="total"> <%= totalCouterTotal %></td>
                </tr>
                <tr>
                    <td class="source2">PMC</td>
                    <td> <%= totalPMCHTML %></td>
                    <td> <%= totalPMCPDF %></td>
                    <td>n.a.</td>
                    <td class="total"> <%= totalPMCTotal %></td>
                </tr>
                <tr>
                    <td>Totals</td>
                    <td class="total"> <%= totalHTML %></td>
                    <td class="total"> <%= totalPDF %>
                    </td>
                    <td class="total"> <%= totalXML %>
                    </td>
                    <td class="total"> <%= total %></td>
                </tr>
                <tr class="percent">
                    <td colspan="5"><b> <%= totalViewsPDFDownloads %>
                        %</b> of article views led to PDF downloads
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

</script>