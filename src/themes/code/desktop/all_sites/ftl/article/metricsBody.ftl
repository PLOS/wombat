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

<div class="main cf" id="pjax-container">
<#assign tab="metrics" />
<#assign almInfoURL="http://lagotto.io/plos" />

  <#include "metricsTemplates.ftl" />

  <div id="article-metrics" data-showTooltip="true">
    <h2 id="viewedHeader">Viewed <a href="${almInfoURL}#usageInfo" class="ir" title="More information">info</a>
      <span id="chartSpinner"><#include "../common/loadingcycle.ftl"></span>
    </h2>

    <div id="usage"></div>
    <div id="views"></div>

    <a id="citations" name="citations"></a>
    <h2 id="citedHeader" class="topstroke">Cited <a href="${almInfoURL}#citationInfo" class="ir"
                                                    title="More information">info</a>
      <span id="relatedCitesSpinner"><#include "../common/loadingcycle.ftl"></span>
    </h2>
    <div id="relatedCites"></div>

    <div id="socialNetworksOnArticleMetricsPage">
      <a id="other" name="other"></a>
      <h2 id="savedHeader" class="topstroke">Saved <a href="${almInfoURL}#socialBookmarks" class="ir"
                                                      title="More information">info</a>
        <span id="relatedBookmarksSpinner"><#include "../common/loadingcycle.ftl"></span>
      </h2>
      <div id="relatedBookmarks"></div>
    </div>

    <h2 id="discussedHeader" class="topstroke">Discussed <a href="${almInfoURL}#blogCoverage" class="ir"
                                                            title="More information">info</a>
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
            </#if>-->

    <@siteLink handlerName="articleComments" queryParameters={"id":articleDoi}; commentsTabURL>
      <div id="notesAndCommentsOnArticleMetricsTab" class="metrics_tile">
        <a href="${commentsTabURL}">
          <img id="notesAndCommentsImageOnArticleMetricsTab"
               src="<@siteLink handlerName="staticResource" wildcardValues=["img/logo-comments.png"]/>"
               alt="${commentCount.all} Comments and Notes" class="metrics_tile_image"/>
        </a>

        <div class="metrics_tile_footer" onclick="location.href='${commentsTabURL}';">
          <a href="${commentsTabURL}">${commentCount.all}</a>
        </div>
      </div>
    </@siteLink>
    </div>

    <h2 id="f1kHeader" class="topstroke" style="display: none;">Recommended <a href="${almInfoURL}#recommended"
                                                                               class="ir"
                                                                               title="More information">info</a>
      <span id="f1KSpinner"><#include "../common/loadingcycle.ftl"></span>
    </h2>
    <div id="f1kContent" style="display:none;"></div>

    <!-- try to find the location of the first '/' after 'http://' -->
  <#--    <#assign endIndex = freemarker_config.get("almHost")?index_of("/", 7)>
      <#if endIndex == -1>
        <#assign endIndex = freemarker_config.get("almHost")?length >
      </#if>-->
    <div><a href="https://plos.org/article-level-metrics">Information on PLOS Article-Level Metrics</a></div>
  <#include "metricsFeedbackMessage.ftl" />
  </div><!--end article-metrics-->
</div><!-- end main -->