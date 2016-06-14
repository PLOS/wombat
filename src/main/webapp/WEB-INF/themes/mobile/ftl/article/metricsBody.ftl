<#assign almInfoURL="http://lagotto.io/plos" />

<div id="article-metrics">

<#include "../common/article/metricsTemplates.ftl" />

  <section id="viewedCard" class="card">

    <h2 id="viewedHeader">Viewed <a href="${almInfoURL}#usageInfo" class="ir" title="More information"><i
        class="fa fa-question-circle"></i></a>
    </h2>

    <div id="usage"></div>
    <div id="views"></div>
  </section>
  <section class="card">
    <a id="citations" name="citations"></a>

    <h2 id="citedHeader">Cited <a href="${almInfoURL}#citationInfo" class="fa" title="More information"><i
        class="fa fa-question-circle"></i></a>
    </h2>
    <div id="relatedCites"></div>
  </section>

  <section id="savedCard" class="card">

    <div id="socialNetworksOnArticleMetricsPage">
      <a id="other" name="other"></a>
      <h2 id="savedHeader">Saved <a href="${almInfoURL}#socialBookmarks" class="ir" title="More information"><i
          class="fa fa-question-circle"></i></a>
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
        ${commentCount.root}
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
  <section class="card">
    <p>
      <a href="${almInfoURL}#static-content-wrap">Information on PLOS Article-Level Metrics</a>
    </p>

    <p>
      Questions or concerns about usage data? <a href="<@siteLink handlerName="feedback" />">Please let us know.</a>
    </p>

  </section>
</div>
