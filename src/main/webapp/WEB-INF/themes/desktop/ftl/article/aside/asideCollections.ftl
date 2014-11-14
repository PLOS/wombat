
<div class="aside-container">
  <h3>Included in the Following Collection</h3>
  <ul id="collectionList">
  <#list collectionIssues?keys as issueDoi>
    <li>
     <a href="<@siteLink path="article/browse/issue/info:doi/" + issueDoi
                         journalKey=collectionIssues[issueDoi]["parentJournal"]["journalKey"]/>">
        ${collectionIssues[issueDoi]["displayName"]}</a>
    </li>
  </#list>
  </ul>
</div>