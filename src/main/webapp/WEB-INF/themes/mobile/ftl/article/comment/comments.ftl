<#include "../../common/htmlTag.ftl" />

<#assign title = "" />
<#include "../../common/head.ftl" />

<body id="page-comments">
<div id="container-main">
<#include "../backToArticle.ftl" />

  <div id="comment-content" class="content">
    <h3 class="comments-header">Reader Comments (${article.commentCount.root})</h3>

  <#-- TODO: implement when we support logged-in functionality.
  <p class="post-comment"><a href="FIXME">Post a comment</a> on this article.</p>
  -->

  <#if userApiError??>
    <#include "userApiErrorMessage.ftl" />
  <#else>
    <#include "commentsBody.ftl" />
  </#if>
  </div><#--end content-->

<#include "../../common/footer/footer.ftl" />

</div><#--end container main-->

<#include "../../common/bodyJs.ftl" />
</body>
</html>
