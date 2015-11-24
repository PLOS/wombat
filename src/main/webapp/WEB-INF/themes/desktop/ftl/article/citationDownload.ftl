<html>
<#assign title = "" /> <#-- use default -->
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<div>
  <h1>Download Citation</h1>

  <div class="source">

    <span>Article Source:</span>
    <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />"
       title="Back to original article">
    ${article.title}
    </a>

    <p>
    <#include "citation.ftl" />
    <@displayCitation article false />
    </p>

    <h2>Download the article citation in the following formats:</h2>
    <ul>
      <li>
        <a href="<@siteLink handlerName="downloadRisCitation" queryParameters={'id': article.doi} />"
           title="RIS Citation">
          RIS
        </a> (compatible with EndNote, Reference Manager, ProCite, RefWorks)
      </li>
      <li>
        <a href="<@siteLink handlerName="downloadBibtexCitation" queryParameters={'id': article.doi} />"
           title="BibTex Citation">
          BibTex
        </a> (compatible with BibDesk, LaTeX)
      </li>
    </ul>
  </div>
</div>

<#include "../common/footer/footer.ftl" />
</body>
</html>
