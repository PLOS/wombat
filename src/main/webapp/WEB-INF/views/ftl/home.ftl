<#include "common/htmlTag.ftl" />

<#include "common/title/siteTitle.ftl" />
<#assign title = siteTitle />
<#assign depth = 0 />
<#include "common/head.ftl" />

<body>
<#include "common/header.ftl" />

<div id="home-content" class="content">

  <section id="home-articles" class="articles-container">
    <div id="article-results-container">
    <#if articles?? >
      <section>
        <ul id="article-results" class="results">
          <#list articles.docs as article>
            <li>
              <a href="article?doi=${article.id}">${article.title}</a>
            </li>
          </#list>
        </ul>
      </section>
    <#else>
      <div>Article list unavailable.</div><#-- TODO: Friendlier message? -->
    </#if>
    </div>
  </section>

<#include "common/footer/footer.ftl" />
</body>
</html>
