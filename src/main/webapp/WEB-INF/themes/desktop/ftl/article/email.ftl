<html xmlns="http://www.w3.org/1999/html">
<#assign title = article.title, articleDoi = article.doi />
<#assign cssFile = 'site-content.css'/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../macro/doiResolverLink.ftl" />

<body class="${journalStyle}">

  <#include "../common/header/headerContainer.ftl" />
  <#include  "emailArticleBody.ftl" />
  <#include "../common/footer/footer.ftl" />

  <@renderJs />
</body>
</html>
