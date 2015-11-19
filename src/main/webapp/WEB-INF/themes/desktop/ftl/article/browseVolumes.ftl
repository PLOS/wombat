<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = '' />
<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<!-- TODO: This is how Ambra does it, but it would be much preferred to provide the proper thumbnail asset file ids
      for each issue in the controller layer. -->
<#assign thumbnail_suffix = ".g001.PNG_S"/>

<div class="set-grid">

    <section class="article-body">


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
          </div><#-- journal_issues -->
      </#if>

    </#if>
    </section>
</div>

<#include "../common/footer/footer.ftl" />
<@js src="resource/js/components/browse_volumes.js"/>
<@renderJs />

<@cssLink target="resource/css/browse-volumes.css"/>
<@renderCssLinks />

</body>
</html>
