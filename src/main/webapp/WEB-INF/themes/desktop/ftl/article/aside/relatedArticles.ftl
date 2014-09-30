
<#if article.relatedArticles?size gt 0>

<div class="related-articles-container">
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  <ul>
  <#list article.relatedArticles as relatedArticle>
      <li>
        <a href="<@siteLink path="article?id=" + relatedArticle.doi />">
      ${relatedArticle.title}  a title goes here
        </a>
      </li>
  </#list>
  </ul>
</div>

</#if>
