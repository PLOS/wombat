<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">

  <#include "../common/header/header.ftl" />
  <div class="plos-row">
    <section class="article-body">
      <div class="classifications">
        <p class="license-short" id="licenseShort"><span class="icon"></span>Open Access</p>
        <p class="peer-reviewed" id="peerReviewed"><span class="icon"></span>Peer-reviewed</p>
        <div class="article-type" id="articleType">${article.articleType!""}</div>
      </div>

      <div id="articleText">
      <h1>${article.title}</h1>

      ${articleText}
      </div>
    </section>
    <aside class="stub article-column">

    </aside>
  </div>
  <#include "../common/footer/footer.ftl" />

  <@renderJs />
  </body>
</html>
