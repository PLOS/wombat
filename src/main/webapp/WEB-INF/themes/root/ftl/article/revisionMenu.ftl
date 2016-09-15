
<div id="revisionMenu">
  <form>
   <select name="revisionLink" id="revisionLink">
     <#list revisionMenu as revisionNumber>
       <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revisionNumber?c}
          ; href>
        <option value="${href}" <#if articlePtr.rev?? && revisionNumber?c == articlePtr.rev>selected</#if>>
          Version ${revisionNumber}
        </option>
      </@siteLink>
  </#list>
    </select>
  </form>
</div>
