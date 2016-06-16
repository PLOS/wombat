<#include "../../baseTemplates/articleSection.ftl" />
<#assign bodyId = 'page-comments' />
<#assign mainId = "comment-content" />
<#assign mainClass = "content" />

<@page_header />
<h3 class="comments-header">Reader Comments (${commentCount.root})</h3>

<#-- TODO: implement when we support logged-in functionality.
<p class="post-comment"><a href="FIXME">Post a comment</a> on this article.</p>
-->

<#if userApiError??>
  <#include "userApiErrorMessage.ftl" />
<#else>
  <#include "commentsBody.ftl" />
</#if>
<@page_footer />
