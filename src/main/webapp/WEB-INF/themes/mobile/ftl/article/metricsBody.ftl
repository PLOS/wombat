<div id="pjax-container">
<#assign almInfoURL="http://lagotto.io/plos" />

  <div id="article-metrics">

    <script type="text/template" id="metricsTileTemplate">
      <div id="<%= name %>OnArticleMetricsTab" class="metrics_tile">
        <h3><a href="<%= url %>"><%= name %></a></h3>
        <div class="metrics_tile_footer" onclick="location.href=<%= url %>;">
          <%= linkText %>
        </div>
      </div>
    </script>

    <script type="text/template" id="metricsTileTemplateNoLink">
      <div id="<%= name %>OnArticleMetricsTab" class="metrics_tile">
        <h3><%= name %></h3>
        <div class="metrics_tile_footer">
          <%= linkText %>
        </div>
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
            <td class="data1"> <%= likes.format(0, '.', ',') %> </td>
            <td class="data2"> <%= shares.format(0, '.', ',') %> </td>
            <td class="data1"> <%= comments.format(0, '.', ',') %> </td>
          </tr>
          </tbody>
        </table>
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
        <div id="left">
          <table id="pageViewsTable">
            <tbody>
            <tr>
              <th nowrap="" colspan="2">HTML Page Views</th>
            </tr>
            <tr>
              <td class="source1">PLOS</td>
              <td> <%= totalCounterHTML %></td>
            </tr>
            <tr>
              <td class="source2">PMC</td>
              <td> <%= totalPMCHTML %></td>
            </tr>
            <tr>
              <td>Totals</td>
              <td class="total"> <%= totalHTML %></td>
            </tr>
            </tbody>
          </table>
          <table id="pageViewsTable">
            <tbody>
            <tr>
              <th nowrap="" colspan="2">PDF Downloads</th>
            </tr>
            <tr>
              <td class="source1">PLOS</td>
              <td> <%= totalCounterPDF %></td>
            </tr>
            <tr>
              <td class="source2">PMC</td>
              <td> <%= totalPMCPDF %></td>
            </tr>
            <tr>
              <td>Totals</td>
              <td class="total"><%= totalPDF %></td>
            </tr>
            </tbody>
          </table>
          <table id="pageViewsTable">
            <tbody>
            <tr>
              <th nowrap="" colspan="2">XML Downloads</th>
            </tr>
            <tr>
              <td class="source1">PLOS</td>
              <td> <%= totalCounterXML %></td>
            </tr>
            <tr>
              <td class="source2">PMC</td>
              <td>n.a.</td>
            </tr>
            <tr>
              <td>Totals</td>
              <td class="total"> <%= totalXML %></td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

    </script>

    <section id="viewedCard" class="card">

      <h2 id="viewedHeader">Viewed <a href="${almInfoURL}#usageInfo" class="ir" title="More information"><i class="fa fa-question-circle"></i></a>
      </h2>

      <div id="usage"></div>
      <div id="views"></div>
    </section>
    <section class="card">
      <a id="citations" name="citations"></a>

      <h2 id="citedHeader">Cited <a href="${almInfoURL}#citationInfo" class="fa" title="More information"><i class="fa fa-question-circle"></i></a>
      </h2>
      <div id="relatedCites"></div>
    </section>

    <section class="card">

      <div id="socialNetworksOnArticleMetricsPage">
        <a id="other" name="other"></a>
        <h2 id="savedHeader">Saved <a href="${almInfoURL}#socialBookmarks" class="ir" title="More information"><i class="fa fa-question-circle"></i></a>
        </h2>
        <div id="relatedBookmarks"></div>
      </div>
    </section>

    <section class="card">

      <h2 id="discussedHeader">Discussed <a href="${almInfoURL}#blogCoverage" class="ir"
                                            title="More information"><i class="fa fa-question-circle"></i></a>
      </h2>

      <div id="relatedBlogPosts" style="display:none;">
      <#-- @TODO: Waiting for trackback service in order to implement. -->
      <#--        <#if trackbackCount gt 0>
                  <div id="trackbackOnArticleMetricsTab" class="metrics_tile">
                    <@s.a href="${relatedTabURL}#trackbackLinkAnchor">
                        <img id="trackbackImageOnArticleMetricsTab" src="${freemarker_config.context}/images/logo-trackbacks.png"
                             alt="${trackbackCount} Trackbacks" class="metrics_tile_image"/>
                    </@s.a>
                      <div class="metrics_tile_footer" onclick="location.href='${relatedTabURL}#trackbackLinkAnchor';">
                        <@s.a href="${relatedTabURL}#trackbackLinkAnchor">${trackbackCount}</@s.a>
                      </div>
                  </div>
              </#if>-->

      <@siteLink handlerName="articleComments" queryParameters={"id":articleDoi}; commentsUrl>
        <div id="commentsOnArticleMetricsTab" class="metrics_tile">
          <h3><a href="${commentsUrl}">Comments and Notes</a></h3>
          <div class="metrics_tile_footer" onclick="location.href=${commentsUrl};">
          ${article.commentCount.root}
          </div>
        </div>
      </@siteLink>
      </div>
    </section>

    <section class="card" id="f1kHeader" style="display: none;">

      <h2>Recommended<a href="${almInfoURL}#recommended" class="ir"
                                                    title="More information"><i class="fa fa-question-circle"></i></a>
      <#--<span id="f1KSpinner"><#include "../common/loadingcycle.ftl"></span>-->
      </h2>
      <div id="f1kContent" style="display:none;"></div>
    </section>
    <!-- try to find the location of the first '/' after 'http://' -->
  <#--    <#assign endIndex = freemarker_config.get("almHost")?index_of("/", 7)>
      <#if endIndex == -1>
        <#assign endIndex = freemarker_config.get("almHost")?length >
      </#if>-->
    <div class="card"><p></p><a href="${almInfoURL}#static-content-wrap">Information on PLOS Article-Level Metrics</a></p>

    <p>Questions or concerns about usage data? <a href="<@siteLink handlerName="feedback" />">Please let us know.</a></p>

    </div><!--end article-metrics-->
</div><!-- end main -->
