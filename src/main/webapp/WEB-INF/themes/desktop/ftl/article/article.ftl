<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />
<#assign rawPubDate = article.date />

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">

  <#include "../common/header/header.ftl" />

  <section class="article-body">
    <div class="classifications">
      <p class="license-short" id="licenseShort"><span class="icon"></span>Open Access</p>

      <#if article.title=="Research Article">
      <p class="peer-reviewed" id="peerReviewed"><span class="icon"></span>Peer-reviewed</p>
      </#if>

      <div class="article-type" id="articleType">${article.articleType!""}</div>
    </div>

    <div id="articleText">
    ${article.title}

      <p>Published date: ${article.date}</p>

    ${articleText}
    </div>
  </section>
  <aside class="stub article-column">



  </aside>
  <#include "../common/footer/footer.ftl" />

  <@renderJs />
  </body>
</html>

str = "2008-09-26T07:00:00Z";
ending = str.indexOf('T');
modified = str.substring(0, ending);
shoe = new Date(modified);
