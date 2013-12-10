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
      <section>
        <ul id="article-results" class="results">
          <li>
            <div class="error">
              Our system is having a bad day. Please check back later.
            </div>
          </li>
        </ul>
      </section>
    </#if>
    </div>
  </section>

<#include "common/footer/footer.ftl" />
</body>
</html>
