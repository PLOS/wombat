<#if (article.articleType?? && article.articleType.heading?has_content) >
  <#assign articleTypeHeading = article.articleType.heading />
<#else>
  <#assign articleTypeHeading = "unclassified" />
</#if>

<#if (article.articleType?? && article.articleType.code?has_content) >
  <#assign partialArticleTypeLink = "#" + article.articleType.code />
<#else>
  <#assign partialArticleTypeLink = '' />
</#if>
