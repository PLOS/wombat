<h1>Journal Archive</h1>
<#if journal??>
  <#if journal.currentIssue??>
  <div class="journal_current">
    <h2>Current Issue</h2>

    <#if journal.currentIssue.imageArticle??>
      <div class="issue_container">
        <div class="journal_thumb">
          <@siteLink handlerName="browseIssues" queryParameters={"id": journal.currentIssue.doi}; issueLink>
            <a href="${issueLink}">
              <img
                  src="<@siteLink handlerName="assetFile" queryParameters={"type": "small", "id": journal.currentIssue.imageArticle.figureImageDoi}/>"
                  class="current-img" alt="Current Issue"/>
              <span>${journal.currentIssue.displayName}</span>
            </a>
          </@siteLink>
        </div>

        <div class="journal_description">
          <p class="tag">ABOUT THIS IMAGE</p>
        ${issueTitle}
        ${issueDescription}
        </div>
      </div> <#--  issue_container -->
    </#if>
  </div><#-- journal_current -->
  </#if>

  <#if journal.volumes??>
  <div class="journal_issues">

    <h3>All Issues</h3>

    <ul id="journal_years">
      <#list journal.volumes?reverse as volume>
        <li class="btn primary"><a href="#${volume.displayName}">
        ${volume.displayName}</a>
        </li>
      </#list>
    </ul>

    <ul id="journal_slides">
      <#list journal.volumes?reverse as volume>
        <li id="${volume.displayName}" class="slide">
          <ul>
            <#assign issues = volume.issues />
            <#list issues as issue>
              <li<#if ((issue_index + 1) % 6) = 0> class="endrow"</#if>>
                <@siteLink handlerName="browseIssues" queryParameters={"id": "${issue.doi}"}; issueLink>
                  <a href="${issueLink}">
                    <#if issue.imageArticle??>
                      <@siteLink handlerName="assetFile" queryParameters={"type": "small", "id": issue.imageArticle.figureImageDoi}; issueImgURL>
                        <img src="${issueImgURL}" alt="${issue.displayName} Journal Cover"/>
                      </@siteLink>
                    </#if>
                    <span>${issue.displayName}</span>
                  </a>
                </@siteLink>
              </li>
            </#list>
          </ul>
        </li>
      </#list>
    </ul>
  </div><#-- journal_issues -->
  </#if>

</#if>
