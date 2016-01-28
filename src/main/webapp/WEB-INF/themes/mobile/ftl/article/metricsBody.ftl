<div class="main cf" id="pjax-container">
<#assign almInfoURL="http://lagotto.io/plos" />

  <div id="article-metrics">

    <script type="text/template" id="metricsTileTemplateBasic">
      <div id=" <%= name %>OnArticleMetricsTab" class="metrics_tile">
        <h3><a href="<%= url %>"><%= name %></a></h3> +
        <div class="metrics_tile_footer" onclick="location.href=<%= url %>;">
          <%= linkText %> </div></div>
    </script>
    <section class="card">

    <h2 id="viewedHeader">Viewed <a href="${almInfoURL}#usageInfo" class="ir" title="More information">info</a>
      <#--<span id="chartSpinner"><#include "../common/loadingcycle.ftl"></span>-->
    </h2>

    <div id="usage"></div>
    <div id="views"></div>
    </section>
    <section class="card">
    <a id="citations" name="citations"></a>

    <h2 id="citedHeader">Cited <a href="${almInfoURL}#citationInfo" class="ir" title="More information">info</a>
      <#--<span id="relatedCitesSpinner"><#include "../common/loadingcycle.ftl"></span>-->
    </h2>
    <div id="relatedCites"></div>
    </section>

    <section class="card">

    <div id="socialNetworksOnArticleMetricsPage">
      <a id="other" name="other"></a>
      <h2 id="savedHeader">Saved <a href="${almInfoURL}#socialBookmarks" class="ir" title="More information">info</a>
        <#--<span id="relatedBookmarksSpinner"><#include "../common/loadingcycle.ftl"></span>-->
      </h2>
      <div id="relatedBookmarks"></div>
    </div>
</section>

    <section class="card">

    <h2 id="discussedHeader">Discussed <a href="${almInfoURL}#blogCoverage" class="ir" title="More information">info</a>
      <#--<span id="relatedBlogPostsSpinner"><#include "../common/loadingcycle.ftl"></span>-->
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

    <#include "../common/legacyLink.ftl" />
                <#--@TODO: replace legacyUrlPrefix with commentsTabURL when wombat's comments tab is ready-->

                <#--<@siteLink handlerName="articleComments" queryParameters={"id":articleDoi}; commentsTabURL>-->
                <#--</@siteLink>-->

    <#--<#assign commentsTabURL = legacyUrlPrefix + "article/comments/info:doi/" + articleDoi/>-->
      <#--<div id="notesAndCommentsOnArticleMetricsTab" class="metrics_tile">-->
        <#--<a href="${commentsTabURL}">-->
          <#--<img id="notesAndCommentsImageOnArticleMetricsTab" src="<@siteLink handlerName="staticResource" wildcardValues=["img/logo-comments.png"]/>"-->
               <#--alt="${articleComments?size} Comments and Notes" class="metrics_tile_image"/>-->
        <#--</a>-->
        <#--<div class="metrics_tile_footer" onclick="location.href='${commentsTabURL}';">-->
          <#--<a href="${commentsTabURL}">${articleComments?size}</a>-->
        <#--</div>-->
      <#--</div>-->
    <#--</div>-->

    <h2 id="f1kHeader" style="display: none;">Recommended <a href="${almInfoURL}#recommended" class="ir" title="More information">info</a>
      <#--<span id="f1KSpinner"><#include "../common/loadingcycle.ftl"></span>-->
    </h2>
    <div id="f1kContent" style="display:none;"></div>
</section>
    <!-- try to find the location of the first '/' after 'http://' -->
  <#--    <#assign endIndex = freemarker_config.get("almHost")?index_of("/", 7)>
      <#if endIndex == -1>
        <#assign endIndex = freemarker_config.get("almHost")?length >
      </#if>-->
    <div class="card"><a href="${almInfoURL}#static-content-wrap">Information on PLOS Article-Level Metrics</a></div>
  <#--<#include "metricsFeedbackMessage.ftl" />-->
  </div><!--end article-metrics-->
</div><!-- end main -->