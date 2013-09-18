<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Comments" />
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-comments">
<div id="container-main">
<#include "backToArticle.ftl" />

  <div id="comment-content" class="content">
    <h3 class="comments-header">Reader Comments(${articleComments?size})</h3>

    <p class="post-comment"><a href="FIXME">Post a comment</a> on this article.</p>
    <section class="comments">
    <#list articleComments as comment>
      <div class="comment">

        <div class="context">
          <a href="comment?uri=${comment.annotationUri}" class="expand">${comment.title}</a>

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
