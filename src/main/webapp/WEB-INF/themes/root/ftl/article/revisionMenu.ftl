<#if revisionNumbers?has_content>
<div id="revisionMenu">
  <ul>
    <#list revisionNumbers as revisionNumber>
      <#assign linkBody>
        Revision ${revisionNumber}
      </#assign>
      <li>
        <#if revisionNumber == currentRevision>
          <strong>${linkBody}</strong>
        <#else>
          <a href="article?id=${article.doi}&amp;r=${revisionNumber}">${linkBody}</a>
        </#if>
      </li>
    </#list>
  </ul>
</div>
</#if>
