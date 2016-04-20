<#assign metricsUrl>
  <@siteLink handlerName="articleMetrics" queryParameters={"id": article.doi} />
</#assign>
<#include "../common/almQueryJs.ftl" />
<@js src="resource/js/components/signposts.js"/>

<ul id="almSignposts" class="signposts">
  <li id="loadingMetrics">
    <p>Loading metrics</p>
  </li>
</ul>

<script type="text/template" id="signpostsGeneralErrorTemplate">
  <li id="metricsError">Article metrics are unavailable at this time. Please try again later.</li>
</script>

<script type="text/template" id="signpostsNewArticleErrorTemplate">
  <li></li><li></li><li id="tooSoon">Article metrics are unavailable for recently published articles.</li>
</script>

<script type="text/template" id="signpostsTemplate">
    <li id="almSaves">
      <%= s.numberFormat(saveCount, 0) %>
      <div class="tools" data-js-tooltip-hover="trigger">
        <a class="metric-term" href="${metricsUrl}#savedHeader">Save</a>
        <p class="saves-tip" data-js-tooltip-hover="target"><a href="${metricsUrl}#savedHeader">Total Mendeley and CiteULike bookmarks.</a></p>
      </div>
    </li>

    <li id="almCitations">
      <%= s.numberFormat(citationCount, 0) %>
      <div class="tools" data-js-tooltip-hover="trigger">
        <a class="metric-term" href="${metricsUrl}#citedHeader">Citation</a>
        <p class="citations-tip" data-js-tooltip-hover="target"><a href="${metricsUrl}#citedHeader">Paper's citation count computed by Scopus.</a></p>
      </div>
    </li>

    <li id="almViews">
      <%= s.numberFormat(viewCount, 0) %>
      <div class="tools" data-js-tooltip-hover="trigger">
        <a class="metric-term" href="${metricsUrl}#viewedHeader">View</a>
        <p class="views-tip" data-js-tooltip-hover="target"><a href="${metricsUrl}#viewedHeader">Sum of PLOS and PubMed Central page views and downloads.</a></p>
      </div>
    </li>

    <li id="almShares">
      <%= s.numberFormat(shareCount, 0) %>
      <div class="tools" data-js-tooltip-hover="trigger">
        <a class="metric-term" href="${metricsUrl}#discussedHeader">Share</a>
        <p class="shares-tip" data-js-tooltip-hover="target"><a href="${metricsUrl}#discussedHeader">Sum of Facebook and Twitter activity.</a></p>
      </div>
    </li>
</script>
