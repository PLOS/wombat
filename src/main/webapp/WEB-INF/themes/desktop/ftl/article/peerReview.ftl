<html>
<#assign title = article.title, articleDoi = article.doi />

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

    <#include "tabs.ftl" />

    <@displayTabList "PeerReview" />
    <h1>SOME TPR HERE:</h1>
    <div>
      ${peerReview}
    </div>
  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>


<#include "../common/footer/footer.ftl" />
<#include "articleJs.ftl" />


<@renderJs />


</body>
</html>
