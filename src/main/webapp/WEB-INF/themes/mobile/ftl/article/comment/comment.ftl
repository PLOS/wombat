<#include "../../common/htmlTag.ftl" />

<#assign title = comment.title />
<#include "../../common/head.ftl" />

<body id="page-comments-individual">
<#include "../../common/bodyAnalytics.ftl" />
<div id="container-main">
  <header id="site-header-container" class="back-header coloration-border-top">
    <span class="back-arrow">Back</span>
    <a class="back" href="<@siteLink path="article?id=${comment.parentArticle.doi}" />">Back to Article</a>
  </header>

<#macro commentBody comment>
  <div class="context">
    <a class="expand">${comment.title}</a>

    <p class="details">
      <#include "userInfoLink.ftl" />
      Posted by <@userInfoLink comment.creator />
      on <@formatJsonDate date="${comment.created}" format="dd MMM yyyy 'at' hh:mm a" /></p>
  </div>

  <div class="response">
    <p>${comment.formatting.bodyWithHighlightedText}</p>

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

<#if userApiError??>
  <#include "userApiErrorMessage.ftl" />
<#else>
  <div id="comment-content" class="content">
    <section id="comments-individual" class="comments">
      <section class="comment primary coloration-border-top">
        <@commentBody comment=comment />
      </section>
    </section>
  </div>
</div>
</#if>
<section id="comment-info-window" class="modal-info-window top" data-method="full">

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content"></div>

</section><#--end model info window-->

<#include "../../common/bodyJs.ftl" />
</body>
</html>
