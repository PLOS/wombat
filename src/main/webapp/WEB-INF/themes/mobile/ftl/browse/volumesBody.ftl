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
