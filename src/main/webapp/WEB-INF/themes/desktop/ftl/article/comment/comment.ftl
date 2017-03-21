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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title, articleDoi = article.doi />
<#assign cssFile="comments.css"/>

<#include "../../common/head.ftl" />
<#include "../../common/journalStyle.ftl" />

<body class="article ${journalStyle}">

<#include "../../common/header/headerContainer.ftl" />
<div class="set-grid">

<#include "../articleHeader.ftl" />
  <section class="article-body">

  <#include "../tabs.ftl" />
  <@displayTabList 'Comments' />

    <div id="thread" class="article-container">
      <div class="loader">  </div>

      <h2>Reader Comments</h2>

    <#include "postNewCommentLink.ftl" />
    <#include "errorMessages.ftl" />
    <@postNewCommentLink article.doi />

      <div id="captchaForm" style="display: none">
        ${captchaHtml}
      </div>

      <div id="respond_prototype" class="reply subresponse cf" style="display: none">
        <h4>Post Your Discussion Comment</h4>

        <div class="reply_content">
        <#include "newCommentForm.ftl" />
          <@newCommentForm false />
        </div>
      </div>

      <div id="report_prototype" class="reply review cf" style="display: none">
        <div class="flagForm">
          <h4>Why should this posting be reviewed?</h4>

          <div class="reply_content">
          <#include "flagPreamble.ftl" />
          </div>

        <@commentErrorMessageBlock>
          <@commentErrorMessage "missingComment">You must say something in your flag comment</@commentErrorMessage>
          <@commentErrorMessage "commentLength">
            Your flag comment is {length} characters long, it can not be longer than {maxLength}.
          </@commentErrorMessage>
        </@commentErrorMessageBlock>

          <form class="cf">
            <fieldset class="">
              <div class="cf">
                <input type="radio" name="reason" value="spam" id="spam" checked/>
                <label for="spam">Spam</label>
              </div>
              <div class="cf">
                <input type="radio" name="reason" value="offensive" id="offensive"/>
                <label for="offensive">Offensive</label>
              </div>
              <div class="cf">
                <input type="radio" name="reason" value="inappropriate" id="inappropriate"/>
                <label for="inappropriate">Inappropriate</label>
              </div>
              <div class="cf">
                <input type="radio" name="reason" value="other" id="other"/>
                <label for="other">Other</label>
              </div>
              <div id="flag_text">
                <textarea placeholder="Add any additional information here..." name="additional_info"></textarea>
              </div>

            <#-- JavaScript fills in these buttons' on-click behaviors when the box appears. -->
              <span class="btn primary btn_submit">submit</span>
              <span class="btn btn_cancel">cancel</span>
            </fieldset>
          </form>
        </div>
        <!--end flagForm-->

        <div class="flagConfirm" style="display: none;">
          <h4>Thank You!</h4>

          <p>Thank you for taking the time to flag this posting; we review flagged postings on a regular basis.</p>
          <span class="close_confirm">close</span>
        </div>
      </div>
      <!--end report_prototype-->

      <div id="responses">

      <#include "../../macro/userSession.ftl" />
      <#include "userInfoLink.ftl" />

      <#assign indentationWidth = 30 />

      <#--
          Global counter, incremented once for each time renderComment is called.
          Guaranteed to be unique only within this page, for HTML and JS purposes.
          Do not pass these IDs anywhere outside the context of a single page rendering.
          We do it this way so that simple integers can be concatenated into HTML attributes;
          the comments' identifying URIs are not suitable for this.
        -->
      <#assign commentId = 0 />

      <#macro renderComment comment depth replyTo>
        <#assign commentId = commentId + 1 />
        <div id="reply-${commentId}"
             class="form-default response <#if depth==0>original</#if>"
             data-uri="${comment.commentUri}"
             data-depth="${depth?c}"
             style="margin-left: ${(depth * indentationWidth)?c}px"
            >

          <div class="info">
            <h3 class="response_title">${comment.title}</h3>
            <h4>
              <#if depth == 0>
                Posted by <@userInfoLink user=comment.creator class="user icon replyCreator" />
              <#else>
                <@userInfoLink user=comment.creator class="user icon replyCreator" />
                replied to
                <@userInfoLink user=replyTo.creator class="user icon repliedTo" />
              </#if>
              on
              <span class="replyTimestamp">
                <strong>
                  <@formatJsonDate date=comment.created format="dd MMM yyyy 'at' HH:mm zzz" />
                </strong>
              </span>
            </h4>
            <#if depth gt 0>
              <div class="arrow"></div>
            </#if>
          </div>

          <div class="response_content">
            <div class="response_body">
              <#if comment.isRemoved >
                <p class="removed_comment_note">This comment has been removed.</p>
              <#else>
                ${comment.formatting.bodyWithHighlightedText}
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
              <div id="competing_interests" class="competing_interests <#if hasCompetingInterest>present<#else>absent</#if>">
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
          </div>

          <#if areCommentsDisabled?? && !areCommentsDisabled>
          <div class="toolbar">
            <#assign userIsLoggedIn = isUserLoggedIn() />
            <@siteLink handlerName="userLogin" queryParameters={"page": getLinkToCurrentPage()
            } ; login>
              <a title="Report a Concern" class="flag toolbar btn"
                <#if userIsLoggedIn>
                 onclick="comments.showReportBox('${commentId?c}'); return false;"
                <#else>
                 href="${login}"
                </#if>
                  >
                report a concern
              </a>
              <a title="Click to respond" class="respond toolbar btn"
                <#if userIsLoggedIn>
                 onclick="comments.showRespondBox('${commentId?c}', ${depth?c}); return false;"
                <#else>
                 href="${login}"
                </#if>
                  >
                respond to this posting
              </a>
            </@siteLink>
          </div>

        <#-- Containers for drop-down boxes. JavaScript inserts a copy of a prototype div when the button is clicked. -->
          <div class="report_container" style="display:none;"></div>
          <div class="respond_container" style="display:none;"></div>
          </#if>
        </div>

        <div class="replies">
          <#list comment.replies as reply>
          <@renderComment comment=reply depth=(depth+1) replyTo=comment />
          </#list>
        </div>
      </#macro>
      <#if userApiError??>
        <#include "userApiErrorMessage.ftl" />
      <#else>
        <@renderComment comment=comment depth=0 replyTo={} />
      </#if>

      </div>

  </section>
  <aside class="article-aside">
  <#include "../aside/sidebar.ftl" />
  </aside>
</div>

<#include "../../common/footer/footer.ftl" />


<#include "../articleJs.ftl" />
<#include "commentSubmissionJs.ftl" />
<@renderJs />


<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>

</body>
</html>
