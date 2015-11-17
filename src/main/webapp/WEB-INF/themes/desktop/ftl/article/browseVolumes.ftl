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
<div class="set-grid">

    <section class="article-body">


        <h1>Journal Archive</h1>

    <#if journal??>
        <div class="journal_current">
            <h2>Current Issue</h2>
            <div class="issue_container">
              <#if issueImage??>
                  <div class="journal_thumb">
                      <a href="TODO"/>
                    <#assign issueImageId = issueImage.figures[0].thumbnails.medium.file />
                      <img src="<@siteLink handlerName="asset" queryParameters={"id": issueImageId}/>"
                           class="current-img" alt="Current Issue"/>
                      </a>
                  </div>
              </#if>

                <div class="journal_description">
                    <span class="tag">ABOUT THIS IMAGE</span>
                    <br/>
                    <p>${currentIssue.description}</p>
                </div>
            </div> <!--  issue_container -->
        </div><!-- /.journal_current -->
    </#if>

    <#if journal.volumes??>
        <div class="journal_issues">

            <h3>All Issues</h3>

            <ul id="journal_years">
              <#list journal.volumes?keys as volumeId>
                  <li class="btn primary"><a href="#${journal.volumes[volumeId].displayName}">
                    ${journal.volumes[volumeId].displayName}</a>
                  </li>
              </#list>
            </ul>

            <ul id="journal_slides">
              <#list journal.volumes?keys as volumeId>
                  <li id="${journal.volumes[volumeId].displayName}" class="slide">
                      <ul>
                        <#assign issues = journal.volumes[volumeId].issues />
                        <#list issues?keys as issueId>
                          <li<#if ((issueId_index + 1) % 6) = 0> class="endrow"</#if>>
                            <@siteLink
                                handlerName="browseIssue"
                                queryParameters={"id": "${issues[issueId].issueUri}"}; imgLink>
                              <a href="${imgLink}">
                                <#if issues[issueId].imageUri??>
                                  <#assign issueImageFileId = issues[issueId].imageUri + ".g001.PNG_S"/>
                                    <@siteLink handlerName="asset" queryParameters={"id": issueImageFileId}; issueImgURL>
                                      <img src="${issueImgURL}"  alt="${issues[issueId].displayName} Journal Cover" />
                                    </@siteLink>
                                </#if>
                                <span>${issues[issueId].displayName}</span>
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



    </section>
</div>

<#include "../common/footer/footer.ftl" />
<@js src="resource/js/components/browse_volumes.js"/>
<@renderJs />

</body>
</html>
