
<div class="related-articles-container">
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  <ul>
  <#list article.relatedArticles?sort_by("doi")?reverse as relatedArticle>
      <li>
        <a href="<@siteLink path="article?id=" + relatedArticle.doi />">
          ${relatedArticle.title}
        </a>
      </li>
  </#list>
  </ul>
</div>

