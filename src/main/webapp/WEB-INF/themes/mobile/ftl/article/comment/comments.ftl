<#include "../../baseTemplates/articleSection.ftl" />

<#macro page_content>
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
</#macro>

<@render_page '' '' 'page-comments' />
