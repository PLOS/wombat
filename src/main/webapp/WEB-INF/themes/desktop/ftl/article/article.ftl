<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />
<#assign articleType = article.articleType />
<#macro setArticleType classType>
<br/>
  <div class="${classType}" id="artType">
    ${articleType}
  </div>
</#macro>

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">

  <input type="hidden" id="rawPubDate" value="${article.date}" />

  <#include "../common/header/header.ftl" />
  <div class="plos-row">
    <section class="article-body">
     <#include "article-classifications.ftl" />

        <h1 id="artTitle"> ${article.title} </h1>
    <#include "authorList.ftl" />
      <ul class="date-doi">
        <li id="artPubDate">Published:  </li>
        <li id="artDoi">DOI: ${article.doi} </li>
      </ul>

      <#include "tabs.ftl" />

      <div class="article-text" id="artText">
         ${articleText}
      </div>


    </section>
    <aside class="article-column">

    </aside>
  </div>
  <#include "../common/footer/footer.ftl" />

  <@renderJs />
  <script src="<@siteLink path="resource/js/components/dateparse.js"/>"></script>
  <script src="<@siteLink path="resource/js/pages/article.js"/>"></script>

  </body>
</html>
