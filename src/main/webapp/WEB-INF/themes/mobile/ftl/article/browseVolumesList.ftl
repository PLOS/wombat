<ul id="journal_slides">
<#list journal.volumes?reverse as volume>
  <li id="${volume.displayName}" class="slide">
    <ul>
      <#assign issues = volume.issues />
      <#list issues as issue>
        <li>
          <@siteLink
          handlerName="browseIssues"
          queryParameters={"id": "${issue.issueUri}"}; issueLink>
            <a href="${issueLink}">
             ${issue.displayName}
            </a>
          </@siteLink>
        </li>

      </#list>
    </ul>
  </li>
</#list>
</ul>
