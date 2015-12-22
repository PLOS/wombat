<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title />
<#assign articleDoi = article.doi />
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

      <h2>Reader Comments (${articleComments?size})</h2>

      <p class="post_comment">
        <a href="<@siteLink handlerName="articleCommentForm" queryParameters={"id": article.doi} />">
          Post a new comment
        </a>
        on this article
      </p>

      <ul id="threads">
      <#list articleComments?sort_by("mostRecentActivity")?reverse as comment>
        <li class="cf">
          <div class="responses">
            <span>${comment.replyTreeSize}</span>
            <#if comment.replyTreeSize == 1>Response<#else>Responses</#if>
          </div>
          <div class="recent">
            <@formatJsonDate date=comment.mostRecentActivity format="dd MMM yyyy '<br/>' HH:mm zzz" /><br/>
            <span>Most Recent</span>
          </div>
          <div class="title">
            <a href="<@siteLink handlerName="articleCommentTree" queryParameters={"id": comment.annotationUri} />">
            ${comment.title}
            </a>
            <span>
              <#include "userInfoLink.ftl" />
              Posted by <@userInfoLink comment.creator /> on
              <@formatJsonDate date=comment.created format="dd MMM yyyy 'at' HH:mm zzz" />
            </span>
          </div>
        </li>
      </#list>
      </ul>

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
