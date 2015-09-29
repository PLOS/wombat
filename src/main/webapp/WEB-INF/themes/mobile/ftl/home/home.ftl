<#include "../common/htmlTag.ftl" />

<#assign title = '' />
<#assign depth = 0 />
<#include "../common/head.ftl" />

<body>
<div id="container-main">

<#include "../common/header/headerContainer.ftl" />

  <div id="home-content" class="content">

    <section id="home-articles" class="articles-container">

    <#if supportedSections?size gt 1>
      <nav id="article-type-menu" class="menu-rounded">
        <ul>
          <#macro sectionLink sectionName sectionLabel>
            <#if supportedSections?seq_contains(sectionName)>
              <li class="${(selectedSection == sectionName)?string('active color-active', 'color-active')}"
                  data-method="${sectionName}">
              ${sectionLabel}
              </li>
            </#if>
          </#macro>
          <@sectionLink "recent" "recent" />
          <@sectionLink "popular" "popular" />
          <#include "curatedArticleLists.ftl" />
        </ul>
      </nav>
      <form id="hpSectionForm" action="" method="get" style="display: none;">
        <input type="hidden" name="section" id="section" value="${selectedSection}"/>
      </form>
    </#if>

      <div id="article-results-container">
      <#if sections[selectedSection]??>
        <#assign articles = sections[selectedSection] />
        <section>
          <ul id="article-results" class="results">
            <#list articles as article>
              <li>
                <a href="article?id=${article.doi}">${article.title}</a>
              </li>
            </#list>
          </ul>
        </section>

        <#if selectedSection != "in_the_news">
          <#assign numPages = (articles?size / resultsPerPage)?ceiling />
          <#assign currentPage = (RequestParameters.page!1)?number />
          <#assign path = "" />
          <#include "../common/paging.ftl" />
        </#if>
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
      </div><#-- end article-results-container -->

    </section><#-- end articles-container -->

  <#include "homeContent.ftl" />

  </div><#-- end home-content -->

<#include "../common/footer/footer.ftl" />

</div><#-- end container-main -->

<#include "../common/siteMenu/siteMenu.ftl" />
<#include "../common/bodyJs.ftl" />

</body>
</html>
