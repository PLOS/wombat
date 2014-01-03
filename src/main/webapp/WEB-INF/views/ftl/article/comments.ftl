<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Comments" />
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-comments">
<div id="container-main">
<#include "backToArticle.ftl" />

  <div id="comment-content" class="content">
    <h3 class="comments-header">Reader Comments(${articleComments?size})</h3>

  <#-- TODO: implement when we support logged-in functionality.
  <p class="post-comment"><a href="FIXME">Post a comment</a> on this article.</p>
  -->

  <#include "commentsBody.ftl" />
  </div><#--end content-->

<#include "../common/footer/footer.ftl" />

</div><#--end container main-->

<#include "../common/bodyJs.ftl" />
</body>
</html>
