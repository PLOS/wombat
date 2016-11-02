<#macro postNewCommentLink articleDoi>
<p class="post_comment">
  <#if areCommentsDisabled?? && areCommentsDisabled>
    <#include "commentsDisabledMessage.ftl" />
  <#else>
    <a href="<@siteLink handlerName="articleCommentForm" queryParameters={"id": articleDoi} />">
      Post a new comment</a>
    on this article
  </#if>
</p>
</#macro>
