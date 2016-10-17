<h1>Journal Archive</h1>
<#if currentIssue??>
  <@siteLink handlerName="browseIssues" queryParameters={"id": currentIssue.doi}; issueLink>
    <#assign issueLink = issueLink/>
  </@siteLink>

<div class="journal_current">
  <h2>Current Issue: <a href="${issueLink}">${currentIssue.displayName}</a></h2>

  <div class="issue_container">

    <#if currentIssue.imageArticle??>
      <div class="journal_thumb">
        <p>
          <a href="${issueLink}" class="">
            <img
                src="<@siteLink handlerName="figureImage" queryParameters={"size": "small", "id": currentIssue.imageArticle.figureImageDoi}/>"
                class="center-block" alt="Current Issue"/>
          </a>
        </p>
      ${issueDescription}
      </div>
    </#if>

  </div> <!--  issue_container -->
</div><!-- journal_current -->
</#if>

<div class="journal_issues">

  <h2>All Issues</h2>

  <ul id="journal_years" class="main-accordion accordion">
  <#list volumes?reverse as volume>
    <li class="accordion-item">
      <a class="expander">${volume.displayName}</a>
      <#assign issues = volume.issues />
      <ul class="accordion-content">
        <#list issues as issue>

          <li class="pad-small-y">
            <@siteLink handlerName="browseIssues" queryParameters={"id": "${issue.doi}"}; issueLink>
              <a href="${issueLink}" class="txt-medium">
              ${issue.displayName}
              </a>
            </@siteLink>
          </li>

        </#list>
      </ul>
    </li>
  </#list>
  </ul>
</div>
