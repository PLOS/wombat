<section class="comments">
<#list articleComments as comment>
  <div class="comment">

    <div class="context">
      <#assign reqPath = "comment" />
      <#if mode?? && mode = "corrections">
        <#assign reqPath = "correction" />
      </#if>
      <a href="${reqPath}?id=${comment.annotationUri}" class="expand">${comment.title}</a>

      <p class="details">Posted by ${comment.creator.displayName}
        on <@formatJsonDate date="${comment.created}" format="dd MMM yyyy 'at' hh:mm a" />
      </p>
    </div>

    <#if comment.replyTreeSize &gt; 0>
      <div class="responses">
        <p class="response-header">
          <a>${comment.replyTreeSize}
            <#if comment.replyTreeSize == 1 >
              RESPONSE
            <#else>
              RESPONSES
            </#if>
          </a>
          | <@formatJsonDate date="${comment.mostRecentActivity}" format="dd MMM yyyy 'at' hh:mm a" />
        </p>
      </div>
    </#if>
  </div>
</#list>
</section>

<#include "../../common/bottomMenu/bottomMenu.ftl" />
