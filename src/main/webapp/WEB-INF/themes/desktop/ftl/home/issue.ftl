<#if currentIssue.imageArticle??>
  <a href="<@siteLink handlerName="browseIssues"/>">
  <@siteLink handlerName="assetFile" queryParameters={"type": "medium", "id": currentIssue.imageArticle.figureImageDoi} ; src>
    <img src="${src}" class="current-img" alt="Current Issue"/>
  </@siteLink>
  </a>
</#if>
<#if currentIssue??>
  <p class="boxtitle">
    <a href="<@siteLink handlerName="browseIssues"/>">Current Issue</a>
    <span class="subhead">${currentIssue.displayName} ${currentIssue.parentVolume.displayName}</span>
  </p>
<#else>
  <p>Our system is having a bad day.</p>
</#if>
