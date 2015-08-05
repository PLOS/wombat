<div class="aside-container">
  <h3>Included in the Following <#if article.collections?size == 1>Collection<#else>Collections</#if></h3>
  <ul id="collectionList">
  <#list article.collections as articleCollection>
    <li>
      <#assign collectionLinkPath = "collection/${articleCollection.slug}" /><#-- Placeholder. TODO: Build correct link -->
      <a href="<@siteLink path=collectionLinkPath journalKey=articleCollection.journalKey/>">
      ${articleCollection.title}
      </a>
    </li>
  </#list>
  </ul>
</div>
