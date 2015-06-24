<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign depth = 0 />
<#assign title = "Search Results" />
<#assign cssFile="search-results.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/pages/search_results.js"/>
<@renderJs />

<body class="static ${journalStyle}">

  <#include "../common/header/headerContainer.ftl" />

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
            <p class="search-results-alm" id="search-results-alm-${doc_index}">
              <#--todo: link to old ambra-->
              <a href="article/metrics/info:doi/${doc.id}#views">Views: </a> •
              <a href="article/metrics/info:doi/${doc.id}#citations">Citations: </a> •
              <a href="article/metrics/info:doi/${doc.id}#saves">Saves: </a> •
              <a href="article/metrics/info:doi/${doc.id}#shares">Shares: </a>
              <script type="text/javascript">
                (function ($) {
                  $(this).displayALMSummary('${doc.id}', ${doc_index});
                })(jQuery);
              </script>
            </p>
          </dd>
        </#list>
      </dl>
    </article>
  </main>

  <#include "../common/footer/footer.ftl" />

</body>
</html>
