<h1>Journal Archive</h1>
<#if journal??>
  <#if journal.currentIssue??>
  <div class="journal_current">
    <h2>Current Issue</h2>

    <div class="issue_container">
      <#if journal.currentIssue.imageUri??>
        <div class="journal_thumb">
          <@siteLink
          handlerName="browseIssues"
          queryParameters={"id": journal.currentIssue.issueUri}; issueLink>
            <a href="${issueLink}">
            <#assign issueImageFigureDoi = 'TODO' /><#-- TODO: Retrieve figure DOI from metadata -->
            <img src="<@siteLink handlerName="assetFile" queryParameters={"type": "small", "id": issueImageFigureDoi}/>"
                 class="current-img" alt="Current Issue"/>
            <span>${journal.currentIssue.displayName}</span>
            </a>
          </@siteLink>
        </div>
      </#if>

      <div class="journal_description">
        <p class="tag">ABOUT THIS IMAGE</p>
        ${currentIssueDescription}
      </div>
    </div> <!--  issue_container -->
  </div><!-- journal_current -->
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
                <@siteLink
                handlerName="browseIssues"
                queryParameters={"id": "${issue.issueUri}"}; issueLink>
                  <a href="${issueLink}">
                    <#if issue.imageUri??>
                      <#assign issueImageFigureDoi = 'TODO' /><#-- TODO: Retrieve figure DOI from metadata -->
                      <@siteLink handlerName="assetFile" queryParameters={"type": "small", "id": issueImageFigureDoi}; issueImgURL>
                        <img src="${issueImgURL}"
                             alt="${issue.displayName} Journal Cover"/>
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
