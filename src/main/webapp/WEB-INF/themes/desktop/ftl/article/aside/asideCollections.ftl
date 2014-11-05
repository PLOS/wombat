
<div class="aside-container">
  <h3>Included in the Following Collection</h3>
  <ul id="collectionList">
  <#list collectionIssues?keys as issueDoi>
    <li>
      ${issueDoi}
    </li>
  </#list>
  </ul>
</div>
</#if>