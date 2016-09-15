
<div id="revisionMenu">
  <form>
   <select name="revisionLink" id="revisionLink">
     <#list revisionMenu as revisionNumber>
       <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revisionNumber?c}
          ; href>
        <option value="${href}" <#if revisionNumber?c == articlePtr.rev > selected</#if>
        >V${revisionNumber} <#if article.customMeta?keys?seq_contains('PLOS Publication Stage') && article.customMeta['PLOS Publication Stage']?seq_contains("uncorrected-proof") >
               Uncorrected Proof
        </#if>
        </option>
      </@siteLink>
  </#list>
    </select>
  </form>
</div>
