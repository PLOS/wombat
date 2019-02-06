<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">
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

    <#-- HTML representation of Peer Review History -->
    ${peerReview}

  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>


<#include "../common/footer/footer.ftl" />
<#include "articleJs.ftl" />

<@js src="resource/js/pages/peer_review.js" />

<@renderJs />


</body>
</html>
