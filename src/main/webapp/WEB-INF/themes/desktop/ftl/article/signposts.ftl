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
