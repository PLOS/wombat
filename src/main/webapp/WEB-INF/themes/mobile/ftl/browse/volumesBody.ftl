<!-- TODO: This is how Ambra does it, but it would be much preferred to provide the proper thumbnail asset file ids
      for each issue in the controller layer. -->
<#assign thumbnail_suffix = ".g001.PNG_S"/>

<h1>Journal Archive</h1>
<#if journal??>
  <#if journal.currentIssue??>
  <div class="journal_current">
    <h2>Current Issue</h2>

    <div class="issue_container center-text">
      <#if journal.currentIssue.imageUri??>
        <div class="journal_thumb">
          <@siteLink
          handlerName="browseIssues"
          queryParameters={"id": journal.currentIssue.issueUri}; issueLink>
            <a href="${issueLink}"/>
            <#assign issueImageFileId = journal.currentIssue.imageUri + thumbnail_suffix/>
            <img src="<@siteLink handlerName="asset" queryParameters={"id": issueImageFileId}/>"
                 class="current-img" alt="Current Issue"/>
            <span>${journal.currentIssue.displayName}</span>
            </a>
          </@siteLink>
        </div>
      </#if>

      <div class="journal_description">
        <span class="tag">ABOUT THIS IMAGE</span>
        <br/>

        <p>${journal.currentIssue.description}</p>
      </div>
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

          <li <#if ((issue_index + 1) % 6) = 0> class="endrow"</#if>>
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
<#--<#include "browseVolumesList.ftl" />-->
    </div><#-- journal_issues -->
  </#if>

</#if>
