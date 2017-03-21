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

<#macro searchResultsAlm articleDoi journalKey="">

  <div class="search-results-alm-container">
    <p class="search-results-alm-loading">
      Loading metrics information...
    </p>

    <#assign metricsUrl>
      <#if journalKey?has_content>
        <@siteLink journalKey=journalKey handlerName="articleMetrics" queryParameters={"id": articleDoi} /><#t>
      <#else>
        <@siteLink handlerName="articleMetrics" queryParameters={"id": articleDoi} /><#t>
      </#if>
    </#assign>

    <p class="search-results-alm" data-doi="${articleDoi}">
      <a href="${metricsUrl}#viewedHeader">Views:</a>
      •
      <a href="${metricsUrl}#citedHeader">Citations:</a>
      •
      <a href="${metricsUrl}#savedHeader">Saves:</a>
      •
      <a href="${metricsUrl}#discussedHeader">Shares:</a>
    </p>

    <p class="search-results-alm-error">
      <span class="fa-stack icon-warning-stack">
        <i class="fa fa-exclamation fa-stack-1x icon-b"></i>
        <i class="fa icon-warning fa-stack-1x icon-a"></i>
      </span>Metrics unavailable. Please check back later.
    </p>
  </div>
</#macro>