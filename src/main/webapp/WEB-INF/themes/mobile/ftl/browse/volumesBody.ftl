<!-- TODO: This is how Ambra does it, but it would be much preferred to provide the proper thumbnail asset file ids
      for each issue in the controller layer. -->
<#assign thumbnail_suffix = ".g001.PNG_S"/>

<h1>Journal Archive</h1>
<#if journal??>
  <#if journal.currentIssue??>
    <@siteLink
    handlerName="browseIssues"
    queryParameters={"id": journal.currentIssue.issueUri}; issueLink>
      <#assign issueLink = issueLink/>
    </@siteLink>


  <div class="journal_current">
    <h2>Current Issue: <a href="${issueLink}">${journal.currentIssue.displayName}</a></h2>

    <div class="issue_container">

      <#if journal.currentIssue.imageUri??>
        <div class="journal_thumb">
        <p>
            <a href="${issueLink}" class="">
            <#assign issueImageFileId = journal.currentIssue.imageUri + thumbnail_suffix/>
              <img src="<@siteLink handlerName="asset" queryParameters={"id": issueImageFileId}/>"
                 class="center-block"
                 alt="Current Issue"/>
            </a>
        </p>
        ${currentIssueDescription}
        </div>
      </#if>

    </div> <!--  issue_container -->
  </div><!-- journal_current -->
  </#if>

  <#if journal.volumes??>
  <div class="journal_issues">

    <h2>All Issues</h2>

    <ul id="journal_years" class="main-accordion accordion">
      <#list journal.volumes?reverse as volume>
        <li class="accordion-item" >
          <a class="expander">${volume.displayName}</a>


          <#assign issues = volume.issues />
        <ul class="accordion-content">
        <#list issues as issue>

          <li class="pad-small-y">
            <@siteLink
            handlerName="browseIssues"
            queryParameters={"id": "${issue.issueUri}"}; issueLink>
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
  </#if>

</#if>
