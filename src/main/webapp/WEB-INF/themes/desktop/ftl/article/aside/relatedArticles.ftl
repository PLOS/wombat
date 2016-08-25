
<#if article.relatedArticles?size gt 0>

<div class="related-articles-container">
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  <ul>
  <#list article.relatedArticles?sort_by("doi")?reverse as relatedArticle>
      <li>
        <a href="http://dx.doi.org/${relatedArticle.doi}">${relatedArticle.title}</a>
      </li>
  </#list>
  </ul>
</div>

</#if>
