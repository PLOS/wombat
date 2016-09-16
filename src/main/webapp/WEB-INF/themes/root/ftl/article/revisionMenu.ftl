
<div id="revisionMenu">
  <form>
   <select name="revisionLink" id="revisionLink">
     <#list revisionMenu as revision>
       <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revision.revisionNumber?c}
          ; href>
        <option value="${href}" <#if articlePtr.rev?? && revision.revisionNumber?c == articlePtr.rev>selected</#if>>
          Version ${revision.revisionNumber} <#if revision.ingestion.publicationStage?? && revision.ingestion.publicationStage = "vor-update-to-uncorrected-proof">Uncorrected Proof</#if>
        </option>
      </@siteLink>
  </#list>
    </select>
  </form>
</div>
