<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign depth = 0 />
<#assign title = "Search Results" />
<#assign cssFile="search-results.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="static ${journalStyle}">

  <#include "../common/header/header.ftl" />

  <main class="search-results-body">
    <article>
      <dl class="search-results-list">
        <#list searchResults.docs as doc>
          <dt data-doi="${doc.id}"  class="search-results-title">
            <a href="article?id=${doc.id}">${doc.title}</a>
          </dt>
          <dd>
            <p class="search-results-authors">
              <#list doc.author_display![] as author>
                ${author}<#if author_has_next>,</#if>
              </#list>
            </p>
            <#if doc.article_type??>
              ${doc.article_type} |
            </#if>
            published <@formatJsonDate date="${doc.publication_date}" format="dd MMM yyyy" /> |
            <#if doc.cross_published_journal_name??>
              ${doc.cross_published_journal_name[0]}
            </#if>
            <p class="search-results-doi">${doc.id}</p>
          </dd>
        </#list>
      </dl>

      <#assign numPages = (searchResults.numFound / resultsPerPage)?ceiling />
      <#assign currentPage = (RequestParameters.page!1)?number />
      <#assign path = "search" />
      <#include "../common/paging.ftl" />

      <#if numPages gt 1>
        <div class="search-results-num-per-page">
          Results per page
          <div class="search-results-num-per-page-selector">

            <#-- TODO: wire this up so it acutally works.  Waiting to do that until the other
                 form-related widgets (relevance, date, etc) are present.                     -->
            <select name="resultsPerPage" id="resultsPerPage" onchange="this.form.submit()">
              <option value="15" <#if (selectedResultsPerPage == 15)>selected="selected"</#if>>15</option>
              <option value="30" <#if (selectedResultsPerPage == 30)>selected="selected"</#if>>30</option>
              <option value="60" <#if (selectedResultsPerPage == 60)>selected="selected"</#if>>60</option>
            </select>
          </div>
        </div>
      </#if>

    </article>
  </main>

  <#include "../common/footer/footer.ftl" />
  <@renderJs />
</body>
</html>
