<!DOCTYPE html>
<!--[if lt IE 7]>    <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>     <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>     <html class="no-js lt-ie9"> <![endif]-->
<!--[if IE 9]>     <html class="no-js ie9"> <![endif]-->
<!--[if gt IE 9]><!-->
<html class="no-js"> <!--<![endif]-->
<head>
<#include "articleHead.ftl" />
</head>

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

    <nav class="article-menu-bottom small">
      <a class="btn-lg">PLOS Journals</a>
      <a class="btn-lg med">PLOS Blogs</a>

      <div class="btn-top-container">
        <span class="btn-text">Back to Top</span>
        <a class="btn">Back to Top</a>
      </div>
    </nav>

  </div>
  <!--end content-->

  <footer id="common-footer" class="footer">
    <nav class="footer-menu">
      <ul>
        <li>
          <a class="coloration-light-text">About Us</a>
        </li>
        <li>
          <a class="coloration-light-text">Full Site</a>
        </li>
        <li>
          <a class="coloration-light-text">Feedback</a>
        </li>
      </ul>
    </nav>

    <p class="footer-credits">
      <a class="bold">Ambra 2.4.2</a> Managed Colocation provided by <br/><a class="bold">Internet Systems
      Consortium.</a>
    </p>

    <nav class="footer-secondary-menu">
      <ul>
        <li>
          <a>Privacy Policy</a>
        </li>
        <li>
          <a>Terms of Use</a>
        </li>
        <li>
          <a>Advertise</a>
        </li>
        <li>
          <a>Media Inquiries</a>
        </li>
      </ul>
    </nav>
  </footer>

</div>
<!--end container main-->

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js"></script>
<script>window.jQuery || document.write('<script src="js/vendor/jquery-1.9.0.min.js"><\/script>')</script>
<script src="js/navigation.js"></script>
<script src="js/content.js"></script>
</body>
</html>
