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
                <#assign issueImageFileId = issue.imageUri + thumbnail_suffix/>
                <@siteLink handlerName="asset" queryParameters={"id": issueImageFileId}; issueImgURL>
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
