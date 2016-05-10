<#assign cssFile = "feedback.css" />

<#assign title = article.title, articleDoi = article.doi />

<#include "../macro/doiResolverLink.ftl" />
<#include "../common/headContent.ftl" />
<#include  "emailArticleBody.ftl" />
</div> <#-- end home-content -->
<#include "../common/footContent.ftl" />
