<div class="comments-container">
  <h3>Comments</h3>
  <ul>
  <#list articleComments?sort_by("created")?reverse as comment>
    <li>
      <a href="<@siteLink path="article/comment?id=info:doi/" + comment.annotationUri/>">
      ${comment.title}</a>
      Posted by ${comment.creatorDisplayName}
    <#if comment_index == 2>
      <#break>
    </#if>
    </li>
  </#list>
  </ul>
</div>