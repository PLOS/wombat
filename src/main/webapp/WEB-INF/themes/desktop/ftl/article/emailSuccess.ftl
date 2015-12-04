<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

  <h1>Thank You</h1>
  <p><strong>Your e-mail has been sent!</strong></p>

  <div class="source">
    <span>Back to article:</span>
    <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />" title="Back to original article">
      ${article.title}
    </a>
  </div>
</div>

<#include "../common/footer/footer.ftl" />

<@renderJs />

</body>
</html>
