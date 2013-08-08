<!DOCTYPE html>
<!--[if lt IE 7]>    <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>     <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>     <html class="no-js lt-ie9"> <![endif]-->
<!--[if IE 9]>     <html class="no-js ie9"> <![endif]-->
<!--[if gt IE 9]><!-->
<html class="no-js"> <!--<![endif]-->

<#assign title = "PLOS - Comments" />
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-comments">
<div id="container-main">
  <header id="site-header-container" class="back-header coloration-border-top">
    <span class="back-arrow">Back</span>
    <a class="back" href="article?doi=${article.doi}">Back to Article</a>
  </header>
  <!--end header-->

  <div id="comment-content" class="content">
    <h3 class="comments-header">Reader Comments(${articleComments?size})</h3>

    <p class="post-comment"><a href="FIXME">Post a comment</a> on this article.</p>
    <section class="comments">
    <#list articleComments as comment>
      <div class="comment">

        <div class="context">
          <a class="expand">${comment.title}</a>

          <p class="details">Posted by <a class="member">${comment.creatorDisplayName}</a>
            on <@formatJsonDate date="${comment.created}" format="dd MMM yyyy 'at' hh:mm a" />
          </p>
        </div>

        <#if comment.totalNumReplies &gt; 0>
          <div class="responses">
            <p class="response-header">
              <a>${comment.totalNumReplies} RESPONSES</a>
              | <@formatJsonDate date="${comment.lastReplyDate}" format="dd MMM yyyy 'at' hh:mm a" />
            </p>
          </div>
        </#if>
      </div>
    </#list>
    </section>

    <#include "../common/bottomMenu/bottomMenu.ftl" />

  </div>
  <!--end content-->

  <#include "../common/footer/footer.ftl" />

</div>
<!--end container main-->

<#include "../common/bodyJs.ftl" />
</body>
</html>
