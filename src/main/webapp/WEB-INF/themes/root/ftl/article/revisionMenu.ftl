<div id="revisionMenu">
  <ul>
  <#list revisionMenu as revisionNumber>
    <li>
      <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revisionNumber?c}
          ; href>
        <a href="${href}">Revision ${revisionNumber}</a>
      </@siteLink>
    </li>
  </#list>
  </ul>
</div>
