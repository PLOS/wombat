<#if article.relatedArticles?size gt 0>
<div><#-- TODO: Styling -->
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  <#list article.relatedArticles as relatedArticle>
    <div>
      <a href="<@siteLink path="article?id=" + relatedArticle.doi />">
      ${relatedArticle.title}
      </a>
    </div>
  </#list>
</div>
</#if>
