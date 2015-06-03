<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign depth = 0 />
<#assign title = "Search Results" />
<#assign cssFile="search-results.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="static ${journalStyle}">

  <#include "../common/header/header.ftl" />

  <div class="search-results-body">
    <div class="search-results-container">
      <ul class="search-results-list">
      <#list searchResults.docs as doc>
        <li data-doi="${doc.id}">
          <span class="search-results-title"><a href="article?id=${doc.id}">${doc.title}</a></span>
          <span class="search-results-authors">
            <#list doc.author_display![] as author>
              ${author}<#if author_has_next>,</#if>
            </#list>
          </span>
          <#if doc.article_type??>
            ${doc.article_type} |
          </#if>
          published <@formatJsonDate date="${doc.publication_date}" format="dd MMM yyyy" /> |
          <#if doc.cross_published_journal_name??>
            ${doc.cross_published_journal_name[0]}
          </#if>
          <span class="search-results-doi">${doc.id}</span>
        </li>
      </#list>
      </ul>
    </div>
  </div>

  <#include "../common/footer/footer.ftl" />
  <@renderJs />
</body>
</html>
