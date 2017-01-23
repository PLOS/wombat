<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<#include '../baseTemplates/default.ftl' />

<#macro sectionLink sectionName sectionLabel>
  <#if supportedSections?seq_contains(sectionName)>
  <li class="${(selectedSection == sectionName)?string('active color-active', 'color-active')}"
      data-method="${sectionName}">
  ${sectionLabel}
  </li>
  </#if>
</#macro>

<#assign mainId = "home-content" />
<#assign mainClass = "content" />

<@page_header />

<section id="home-articles" class="articles-container">

<#if supportedSections?size gt 1>
  <nav id="article-type-menu" class="menu-rounded">
    <ul>
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

    <#if selectedSection == "recent" || selectedSection == "popular">
      <#assign numPages = (articles?size / resultsPerPage)?ceiling />
      <#assign currentPage = (RequestParameters.page!1)?number />
      <#assign path = "" />
      <#include "../common/paging.ftl" />
      <@paging numPages currentPage path parameterMap />
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

<@page_footer/>


