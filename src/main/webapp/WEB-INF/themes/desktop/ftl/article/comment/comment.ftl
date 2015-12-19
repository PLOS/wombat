<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title, articleDoi = article.doi />
<#assign cssFile="comments.css"/>

<#include "../../common/head.ftl" />
<#include "../../common/journalStyle.ftl" />
<#include "../../common/article/articleType.ftl" />

<#include "../analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">

<#include "../../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "../articleHeader.ftl" />
  <section class="article-body">

  <#include "../tabs.ftl" />
  <@displayTabList 'comments' />

    <div id="thread" class="article-container">

      <h2>Reader Comments</h2>

      <p class="post_comment">
        <a href="<@siteLink handlerName="articleCommentPost" queryParameters={"id": article.doi} />">
          Post a new comment
        </a>
        on this article
      </p>

      <div id="respond_prototype" class="reply subresponse cf" style="display: none">
        <h4>Post Your Discussion Comment</h4>

        <div class="reply_content">
        <#include "newCommentForm.ftl" />
          <@newCommentForm/>
        </div>

      </div>

      <div id="responses">

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
             class="response <#if depth==0>original</#if>"
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
            <div class="response_body">${comment.formatting.bodyWithHighlightedText}</div>

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
          </div>

          <div class="toolbar form-default">
            <#assign userIsLoggedIn = Session["SPRING_SECURITY_CONTEXT"]?exists && Session["SPRING_SECURITY_CONTEXT"].authentication.authenticated />
            <@siteLink handlerName="userLogin" ; login>
              <a title="Report a Concern"
                 class="flag toolbar btn <#if userIsLoggedIn>primary</#if>"
                <#if userIsLoggedIn>
                 onclick="comments.showReportBox('${commentId?c}'); return false;"
                <#else>
                 href="${login}"
                </#if>
                  >
                report a concern
              </a>
              <a title="Click to respond"
                 class="respond toolbar btn <#if userIsLoggedIn>primary</#if>"
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
        </div>

        <div class="replies">
          <#list comment.replies as reply>
          <@renderComment comment=reply depth=(depth+1) replyTo=comment />
          </#list>
        </div>
      </#macro>
      <@renderComment comment=comment depth=0 replyTo={} />

      </div>

  </section>
  <aside class="article-aside">
  <#include "../aside/sidebar.ftl" />
  </aside>
</div>

<#include "../../common/footer/footer.ftl" />



<script type="text/javascript">
  var comments = null;
  (function ($) {
    window.onload = function () {
      comments = new $.fn.comments();
      comments.indentationWidth = ${indentationWidth?c};
      comments.addresses = {
        submitFlagURL: '', // TODO
        submitReplyURL: '', // TODO
        getAnnotationURL: '' // TODO
      };
    };
  }(jQuery));
</script>

<#include "../articleJs.ftl" />
<@js src="resource/js/vendor/jquery.textarea-expander.js" />
<@js src="resource/js/pages/comments.js" />
<@renderJs />


<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

<#include "../aside/crossmarkIframe.ftl" />

</body>
</html>
