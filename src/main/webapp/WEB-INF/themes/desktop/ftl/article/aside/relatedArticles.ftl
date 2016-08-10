
<#if relatedArticles?size gt 0>

<div class="related-articles-container">
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  <ul>
    <#-- TODO: Order by descending publication date -->
  <#list relatedArticles as relatedArticle>
      <li>
        <a href="<@siteLink path="article?id=" + relatedArticle.doi />">
          <@xform xml=relatedArticle.title/>
        </a>
      </li>
  </#list>
  </ul>
</div>

</#if>
