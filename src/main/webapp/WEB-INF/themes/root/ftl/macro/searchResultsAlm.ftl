<#macro searchResultsAlm articleDoi >
  <div class="search-results-alm-container">
    <p class="search-results-alm-loading">
      Loading metrics information...
    </p>

    <#assign metricsUrl>
      <@siteLink handlerName="articleMetrics" queryParameters={"id": articleDoi} />
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