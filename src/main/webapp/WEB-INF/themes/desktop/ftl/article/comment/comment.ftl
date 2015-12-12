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

      <h2>Reader Comments <#--TODO: Do we want to show the count of article's root replies here?--></h2>

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

      <#macro renderComment comment depth replyTo>
        <div class="response <#if depth==0>original</#if>"
             data-depth="${depth?c}"
             style="margin-left: ${(depth * 30)?c}px"
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

          <#if !(comment.competingInterestStatement)??>
          <#--
            If the value is entirely absent, then the comment was created before the user would have been prompted to
            declare whether or not they had any competing interests. Therefore, suppress the competing interests
            element entirely, rather than stating affirmatively that the user has declared no competing interests.
          -->
          <#else>
          <#-- An empty string indicates that the user has affirmatively declared no competing interests. -->
            <#assign hasCompetingInterest = comment.competingInterestStatement?has_content />
            <div class="competing_interests <#if hasCompetingInterest>present<#else>absent</#if>">
              <strong>
                <#if hasCompetingInterest>
                  Competing interests declared:
                <#else>
                  No competing interests declared.
                </#if>
              </strong>
              <#if hasCompetingInterest>
                <span class="ciStmt">${comment.competingInterestStatement}</span>
              </#if>
            </div>
          </div>
          </#if>

          <div class="toolbar">
            <#assign userIsLoggedIn = Session["SPRING_SECURITY_CONTEXT"]?exists && Session["SPRING_SECURITY_CONTEXT"].authentication.authenticated />
            <@siteLink handlerName="userLogin" ; login>
              <a href="${login}" title="Report a Concern"
                 class="flag toolbar btn <#if userIsLoggedIn>primary</#if>"
                 <#if userIsLoggedIn>onclick="<#--TODO-->"</#if>
                  >
                report a concern
              </a>
              <a href="${login}" title="Click to respond"
                 class="respond toolbar btn <#if userIsLoggedIn>primary</#if>"
                 <#if userIsLoggedIn>onclick="<#--TODO-->"</#if>
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


<@js src="resource/js/components/show_onscroll.js"/>
<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js"/>
<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>

<@js src="resource/js/components/twitter_module.js"/>
<@js src="resource/js/components/signposts.js"/>
<@js src="resource/js/components/nav_builder.js"/>
<@js src="resource/js/components/floating_nav.js"/>

<@js src="resource/js/pages/article.js"/>
<@js src="resource/js/pages/article_sidebar.js"/>
<@renderJs />


<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

<#include "../aside/crossmarkIframe.ftl" />

</body>
</html>
