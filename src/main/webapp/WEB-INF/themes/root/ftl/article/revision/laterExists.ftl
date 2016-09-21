<#function showLaterExistsNotice>
  <#return !revisionMenu['isDisplayingLatestRevision'] />
</#function>

<#macro laterExistsNotice>
<p>
  You are currently viewing an older version of this article.
  A <a href="<@siteLink handlerName="article" queryParameters={"id" : article.doi} />">new version</a> is available.
</p>
</#macro>
