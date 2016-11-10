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

<body class="article ${journalStyle}">

<#include "../../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "../articleHeader.ftl" />
  <section class="article-body">

  <#include "../tabs.ftl" />
  <@displayTabList 'Comments' />

    <div id="thread" class="startDiscussion">
      <div class="loader">  </div>

    <#if areCommentsDisabled?? && areCommentsDisabled>
      <#include "commentsDisabledMessage.ftl" />
    <#else>
      <h2>Start a Discussion</h2>

      <div class="reply cf form-default" id="respond">
        <h4>Post Your Discussion Comment</h4>

      <#include "newCommentForm.ftl" />
      <@newCommentForm true />

      </div>
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
