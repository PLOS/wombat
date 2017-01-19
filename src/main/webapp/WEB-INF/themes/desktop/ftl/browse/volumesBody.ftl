<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<h1>Journal Archive</h1>
<#if currentIssue??>
<div class="journal_current">
  <h2>Current Issue</h2>

  <#if currentIssue.imageArticle??>
    <div class="issue_container">
      <div class="journal_thumb">
        <@siteLink handlerName="browseIssues" queryParameters={"id": currentIssue.doi}; issueLink>
          <a href="${issueLink}">
            <img
                src="<@siteLink handlerName="figureImage" queryParameters={"size": "small", "id": currentIssue.imageArticle.figureImageDoi}/>"
                class="current-img" alt="Current Issue"/>
            <span>${currentIssue.displayName}</span>
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

<div class="journal_issues">

  <h3>All Issues</h3>

  <ul id="journal_years">
    <#list volumes?reverse as volume>
      <li class="btn primary"><a href="#${volume.displayName}">
      ${volume.displayName}</a>
      </li>
    </#list>
  </ul>

  <ul id="journal_slides">
    <#list volumes?reverse as volume>
      <li id="${volume.displayName}" class="slide">
        <ul>
          <#list volume.issues as issue>
            <li<#if ((issue_index + 1) % 6) = 0> class="endrow"</#if>>
              <@siteLink handlerName="browseIssues" queryParameters={"id": "${issue.doi}"}; issueLink>
                <a href="${issueLink}">
                  <#if issue.imageArticle??>
                    <@siteLink handlerName="figureImage" queryParameters={"size": "small", "id": issue.imageArticle.figureImageDoi}; issueImgURL>
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
