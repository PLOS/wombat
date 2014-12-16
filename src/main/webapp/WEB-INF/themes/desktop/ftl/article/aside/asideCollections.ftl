
<div class="aside-container">
  <h3>Included in the Following Collection</h3>
  <ul id="collectionList">
  <#list collectionIssues?keys as issueDoi>
    <li>
     <#-- TODO: Implement issue-browsing and point the link to it (this placeholder is currently a 404) -->
     <a href="<@siteLink path="issue?id=" + issueDoi
                         journalKey=collectionIssues[issueDoi]["parentJournal"]["journalKey"]/>">
        ${collectionIssues[issueDoi]["displayName"]}</a>
    </li>
  </#list>
  </ul>
</div>