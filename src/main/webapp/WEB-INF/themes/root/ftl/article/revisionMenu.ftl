<div id="revisionMenu">

${articlePtr.rev}
  <form>
   <select name="revisionLink" id="revisionLink">
  <#list revisionMenu as revisionNumber>
      <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revisionNumber?c}
          ; href>
        <option value="${href}">V${revisionNumber}</option>
      </@siteLink>
  </#list>
    </select>
  </form>
</div>
