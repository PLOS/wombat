<#--
<@s.url id="almInfoURL" namespace="/static" action="almInfo" includeParams="none"/>
<@s.url id="journalStatisticsURL" includeParams="none" namespace="/static" action="journalStatistics"/>
<@s.url action="feedback" namespace="/feedback" includeParams="none" id="feedbackURL"/>
-->

<div class="main cf" id="pjax-container">
<#assign tab="metrics" />
<#assign almInfoURL="/static" />

    <div id="article-metrics">
        <h2 id="viewedHeader">Viewed <a href="${almInfoURL}#usageInfo" class="ir" title="More information">info</a>
            <span id="chartSpinner"><#include "../common/loadingcycle.ftl"></span>
        </h2>

        <div id="usage"></div>
        <div id="views"></div>

        <a id="citations" name="citations"></a>
        <h2 id="citedHeader" class="topstroke">Cited <a href="${almInfoURL}#citationInfo" class="ir" title="More information">info</a>
            <span id="relatedCitesSpinner"><#include "../common/loadingcycle.ftl"></span>
        </h2>
        <div id="relatedCites"></div>

        <div id="socialNetworksOnArticleMetricsPage">
            <a id="other" name="other"></a>
            <h2 id="savedHeader" class="topstroke">Saved <a href="${almInfoURL}#socialBookmarks" class="ir" title="More information">info</a>
                <span id="relatedBookmarksSpinner"><#include "../common/loadingcycle.ftl"></span>
            </h2>
            <div id="relatedBookmarks"></div>
        </div>

        <h2 id="discussedHeader" class="topstroke">Discussed <a href="${almInfoURL}#blogCoverage" class="ir" title="More information">info</a>
            <span id="relatedBlogPostsSpinner"><#include "../common/loadingcycle.ftl"></span>
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
        </#if>

            <div id="notesAndCommentsOnArticleMetricsTab" class="metrics_tile">
            <@s.a href="${commentsTabURL}">
                <img id="notesAndCommentsImageOnArticleMetricsTab" src="${freemarker_config.context}/images/logo-comments.png"
                     alt="${numComments} Comments and Notes" class="metrics_tile_image"/>
            </@s.a>
                <div class="metrics_tile_footer" onclick="location.href='${commentsTabURL}';">
                <@s.a href="${commentsTabURL}">${numComments}</@s.a>
                </div>
            </div>-->
        </div>

        <h2 id="f1kHeader" class="topstroke" style="display: none;">Recommended <a href="${almInfoURL}" class="ir" title="More information">info</a>
            <span id="f1KSpinner"><#include "../common/loadingcycle.ftl"></span>
        </h2>
        <div id="f1kContent" style="display:none;"></div>

        <!-- try to find the location of the first '/' after 'http://' -->
<#--    <#assign endIndex = freemarker_config.get("almHost")?index_of("/", 7)>
    <#if endIndex == -1>
      <#assign endIndex = freemarker_config.get("almHost")?length >
    </#if>-->

        <#include "../common/legacyLink.ftl" />
        <div><a href="${almInfoURL}/#static-content-wrap">Information on PLOS Article-Level Metrics</a></div>
        <div>Questions or concerns about usage data? <a href="${legacyUrlPrefix}feedback/new">Please let us know.</a></div>

    </div><!--end article-metrics-->
</div><!-- end main -->