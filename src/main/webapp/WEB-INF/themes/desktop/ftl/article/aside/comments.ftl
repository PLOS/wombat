<div class="comments-container">
  <h3>Comments</h3>
  <ul>
  <#list articleComments as comment>
      <p><a href="<@siteLink path="article/comment?id=info:doi/" + comment.annotationUri/>">
      ${comment.title}</a>
      Posted by ${comment.creatorDisplayName}</p>
    <#if comment_index == 2>
      <#break>
    </#if>
  </#list>
  </ul>
</div>