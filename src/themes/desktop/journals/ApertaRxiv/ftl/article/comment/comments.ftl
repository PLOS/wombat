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

<!-- reused with modifications from from article/comment/comments.ftl and comment.ftl -->

<#assign title = article.title, articleDoi = article.doi />
<#assign indentationWidth = 30 />

<#--
    Global counter, incremented once for each time renderComment is called.
    Guaranteed to be unique only within this page, for HTML and JS purposes.
    Do not pass these IDs anywhere outside the context of a single page rendering.
    We do it this way so that simple integers can be concatenated into HTML attributes;
    the comments' identifying URIs are not suitable for this.
  -->
<#assign commentId = 0 />

<#include "commentForms.ftl" />

<#macro renderComment comment comment_index comment_has_next depth>
<#assign commentId = commentId + 1 />

<dt id="replytitle-${commentId}" data-commentId="${commentId}"
    class="<#if depth == 0>comment-toggle original </#if>form-default response"
  <#if depth == 0>
    href="#replybody-${commentId}"
  </#if>
>
  <#if depth == 0><#else><div class="arrow"></div></#if>
<span class="response_title">${comment.title}</span>
<span class="response_timestamp">Posted on
  <@formatJsonDate date=comment.created format="dd MMM yyyy 'at' HH:mm zzz" /></span>
</dt>

<dd id="replybody-${commentId}" data-commentId="${commentId}"
    class="<#if depth == 0>comment-box hide </#if>response_content"
>
  <div class="response_body">
    <!-- need this for comments.js within #reply-... item -->
    <span class="response_title" style="display: none;">${comment.title}</span>
    <#if comment.isRemoved >
      <p class="removed_comment_note">This comment has been removed.</p>
    <#else>
    ${comment.formatting.bodyWithHighlightedText}
    </#if>
    <#if comment.authorEmailAddress?has_content>
      <span class="response_author_email_address">${comment.authorEmailAddress}</span>
    </#if>
    <#if comment.authorEmailAddress?has_content && comment.authorName?has_content> - </#if>
    <#if comment.authorName?has_content>
      <span class="response_author_name">${comment.authorName}</span>
    </#if>
  </div>
  <#if !(comment.competingInterestStatement.creatorWasPrompted)>
  <#--
      If the comment was created before the user would have been prompted to declare whether or not they had
      any competing interests, then suppress the competing interests element entirely, rather than stating
      affirmatively that the user has declared no competing interests.
    -->
  <#else>
    <#assign hasCompetingInterest = comment.competingInterestStatement.hasCompetingInterests />
    <div class="competing_interests <#if hasCompetingInterest>present<#else>absent</#if>">
      <strong>
        <#if hasCompetingInterest>
          Competing interests declared:
        <#else>
          No competing interests declared.
        </#if>
      </strong>
      <#if hasCompetingInterest>
        <span class="ciStmt">${comment.formatting.competingInterestStatement}</span>
      </#if>
    </div>
  </#if>

  <#if areCommentsDisabled?? && !areCommentsDisabled>
    <div class="toolbar">
      <a title="Click to respond" class="respond toolbar btn"
         onclick="comments.showRespondBox('${commentId?c}', ${depth?c}, undefined, comments.refreshCallback); return false;">
        Reply to this comment</a>
      <a title="Report a concern" class="flag toolbar btn"
         onclick="comments.showReportBox('${commentId?c}'); return false;">
        Report</a>
    </div>
  </#if>

  <div id="reply-${commentId}"
       data-uri="${comment.commentUri}"
       data-depth="${depth?c}"
  >
    <#-- Containers for drop-down boxes. JavaScript inserts a copy of a prototype div when the button is clicked. -->

    <div class="report_container" style="display:none;"></div>
    <div class="respond_container" style="display:none;"></div>
  </div>

  <dl class="replies" count="${comment.replies?size}">
    <!--<span>${comment.replyTreeSize}</span> <#if comment.replyTreeSize == 1>Response<#else>Responses</#if>-->
    <#list comment.replies as reply>
      <@renderComment comment=reply comment_index=reply_index comment_has_next=reply_has_next depth=(depth+1) />
    </#list>
  </dl>
</dd>
</#macro>

<div>
  <!-- commentCount (${commentCount.root})</h2> -->
<#if userApiError??>
  <#include "userApiErrorMessage.ftl" />
<#else>
  <#include "postNewCommentLink.ftl" />
  <@postNewCommentLink article.doi />

  <div id="reply-0" class="reply" data-uri="${article.doi?js_string}">
    <div class="respond_container" style="display:none;"></div>
  </div>

  <dl class="article-comments" id="threads"
      data-component="collapse" data-toggle="false"
      data-toggle-class="comment-toggle" data-box-class="comment-box">

    <#list articleComments?sort_by("mostRecentActivity")?reverse as comment>
      <@renderComment comment=comment comment_index=comment_index comment_has_next=comment_has_next depth=0 />
    </#list>
  </dl>
</#if>
</div>
