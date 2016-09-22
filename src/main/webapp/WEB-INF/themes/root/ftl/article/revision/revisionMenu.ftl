<#include "revisionLabel.ftl"/>

<!-- make ascending order in the FTL -->
<div id="revisionMenu">
  <form>
   <select name="revisionLink" id="revisionLink">
     <#list revisionMenu.revisions?reverse as revision>
       <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revision.revisionNumber?c}
          ; href>
        <option value="${href}" <#if revision.isDisplayed>selected</#if>>
          Version ${revision.revisionNumber} <@revisionLabel revision/>
        </option>
      </@siteLink>
  </#list>
    </select>
  </form>
</div>
