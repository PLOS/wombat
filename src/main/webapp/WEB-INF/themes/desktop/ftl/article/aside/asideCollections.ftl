
<div class="aside-container">
  <h3>Included in the Following Collection</h3>
  <ul id="collectionList">
  <#list collectionIssues?keys as issueDoi>
    <li>
     <a href="http://www.ploscollections.org/article/browse/issue/info%3Adoi%2F${issueDoi?url}">
        ${collectionIssues[issueDoi]["displayName"]}</a>
    </li>
  </#list>
  </ul>
</div>