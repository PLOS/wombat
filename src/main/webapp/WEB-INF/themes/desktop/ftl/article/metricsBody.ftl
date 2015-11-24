<div class="main cf" id="pjax-container">
<#assign tab="metrics" />
    <div id="article-metrics">
        <h2 id="viewedHeader">Viewed <a href="#usageInfo" class="ir" title="More information">info</a>
            <span id="chartSpinner"><#include "../common/loadingcycle.ftl"></span>
        </h2>

        <div id="usage"></div>
        <div id="views"></div>

        <a id="citations" name="citations"></a>
        <h2 id="citedHeader" class="topstroke">Cited <a href="#citationInfo" class="ir" title="More information">info</a>
            <span id="relatedCitesSpinner"><#include "../common/loadingcycle.ftl"></span>
        </h2>
        <div id="relatedCites"></div>

        <div id="socialNetworksOnArticleMetricsPage">
            <a id="other" name="other"></a>
            <h2 id="savedHeader" class="topstroke">Saved <a href="#socialBookmarks" class="ir" title="More information">info</a>
                <span id="relatedBookmarksSpinner"><#include "../common/loadingcycle.ftl"></span>
            </h2>
            <div id="relatedBookmarks"></div>
        </div>

        <h2 id="discussedHeader" class="topstroke">Discussed <a href="#blogCoverage" class="ir" title="More information">info</a>
            <span id="relatedBlogPostsSpinner"><#include "../common/loadingcycle.ftl"></span>
        </h2>
        <#include "metricsFeedbackMessage.ftl" />
    </div><!--end article-metrics-->
</div><!-- end main -->