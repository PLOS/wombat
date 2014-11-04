<div class="related-articles-container">
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  ${article.relatedArticles.title}
  <ul>
  <#if article.relatedArticles?size gt 1>
    <#list article.relatedArticles?sort_by("doi")?reverse as relatedArticle>
      <li>
        <a href="<@siteLink path="article?id=" + relatedArticle.doi />">
        ${relatedArticle.title}
        </a>
      </li>
    </#list>
  <#else>
    <#list article.relatedArticles as relatedArticle>
      <li>
        <a href="<@siteLink path="article?id=" + relatedArticle.doi />">
        ${relatedArticle.title}
        </a>
      </li>
    </#list>
  </#if>

  </ul>
</div>



