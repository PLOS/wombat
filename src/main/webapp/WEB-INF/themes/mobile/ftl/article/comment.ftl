<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Individual Comment" />
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-comments-individual">
<div id="container-main">
  <header id="site-header-container" class="back-header coloration-border-top">
    <span class="back-arrow">Back</span>
    <a class="back" href="${pathUp(1 "article?doi=${articleDoi}")}">Back to Article</a>
  </header>

<#macro commentBody comment>
  <div class="context">
    <a class="expand">${comment.title}</a>

    <p class="details">Posted by ${comment.creatorDisplayName}
      on <@formatJsonDate date="${comment.created}" format="dd MMM yyyy 'at' hh:mm a" /></p>
  </div>

  <div class="response">
    <p>${comment.body}</p>

    <div class="response-menu">
    <#-- TODO: uncomment when we allow logged-in functionality
      <a class="flag-link">Flag for Removal</a>
      <a class="respond-link">Respond To This Post</a>
      -->
    </div>

  </div>

  <#list comment.replies as reply>
    <section id="comments-related" class="thread-container">
      <div class="thread-level one">
        <div class="comment">
          <@commentBody comment=reply />
        </div>
      </div>
    </section>
  </#list>
</#macro>

  <div id="comment-content" class="content">
    <section id="comments-individual" class="comments">
      <section class="comment primary coloration-border-top">
      <@commentBody comment=comment />
      </section>
    </section>
  </div>
</div>
<section id="comment-info-window" class="modal-info-window top" data-method="full">

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content"></div>

</section><#--end model info window-->

<#include "../common/bodyJs.ftl" />
</body>
</html>
