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

<#assign almInfoURL="http://lagotto.io/plos" />

<div id="article-metrics">

<#include "metricsTemplates.ftl" />

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

  <section class="card" id="f1kHeader">


    <h2>Recommended<a href="${almInfoURL}#recommended" class="ir"
                      title="More information"><i class="fa fa-question-circle"></i></a>
    <#--<span id="f1KSpinner"><#include "../common/loadingcycle.ftl"></span>-->
    </h2>
    <div id="f1kContent" ></div>
  </section>
  <!-- try to find the location of the first '/' after 'http://' -->
<#--    <#assign endIndex = freemarker_config.get("almHost")?index_of("/", 7)>
    <#if endIndex == -1>
      <#assign endIndex = freemarker_config.get("almHost")?length >
    </#if>-->
  <section class="card">
    <p>
      <a href="https://plos.org/article-level-metrics">Information on PLOS Article-Level Metrics</a>
    </p>

    <p>
      Questions or concerns about usage data? <a href="<@siteLink handlerName="feedback" />">Please let us know.</a>
    </p>

  </section>
</div>
