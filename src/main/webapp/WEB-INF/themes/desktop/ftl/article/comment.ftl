<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title, articleDoi = article.doi />

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />
  <@displayTabList 'comments' />

    <div class="article-container">

      <h2>Reader Comments <#--TODO: Do we want to show the count of article's root replies here?--></h2>

      <p class="post_comment">
        <a href="<@siteLink handlerName="articleCommentPost" queryParameters={"id": article.doi} />">
          Post a new comment
        </a>
        on this article
      </p>

      <div id="responses">

      <#macro userReference user class="">
        <a href="" class="${class}"><#-- TODO: Link to user profile page -->
              ${user.displayName}
        </a>
      </#macro>

      <#macro renderComment comment depth replyTo>
        <div class="response <#if depth==0>original</#if>" data-depth="${depth?c}">

          <div>
            <h3>${comment.title}</h3>
            <h4>
              <#if depth == 0>
                Posted by <@userReference comment.creator />
              <#else>
                <@userReference comment.creator /> replied to <@userReference replyTo.creator />
              </#if>
              on
              <@formatJsonDate date=comment.created format="dd MMM yyyy 'at' HH:mm zzz" />
            </h4>
          </div>

          <div class="content">
            <div class="body">${comment.formatting.bodyWithHighlightedText}</div>
          </div>

          <div class="toolbar">
            <#assign userIsLoggedIn = Session["SPRING_SECURITY_CONTEXT"]?exists && Session["SPRING_SECURITY_CONTEXT"].authentication.authenticated />
            <@siteLink handlerName="userLogin" ; login>
              <a href="${login}" title="Report a Concern"
                 class="flag btn <#if userIsLoggedIn>primary</#if>"
                 <#if userIsLoggedIn>onclick="<#--TODO-->"</#if>
                  >
                report a concern
              </a>
              <a href="${login}" title="Click to respond"
                 class="respond btn <#if userIsLoggedIn>primary</#if>"
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
  <#include "aside/sidebar.ftl" />
  </aside>
</div>

<#include "../common/footer/footer.ftl" />


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

<#include "aside/crossmarkIframe.ftl" />

</body>
</html>
