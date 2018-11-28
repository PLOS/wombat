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
<#assign title = 'Results' />
<#assign bodyId = 'page-results' />

<@page_header />
<div id="filter-results-container" class="filter-box coloration-white-on-color" data-function="date-and-sort">
  <form id="sortAndFilterSearchResults"
  <#if isBrowse??>
        action="<@siteLink handlerName='browseSubjectArea' pathVariables={"subject":subject}/>"
  <#else>
        action="<@siteLink handlerName='simpleSearch'/>"
  </#if>
        method="get">
  <#if RequestParameters.q??>
    <input type="hidden" name="q" value="${RequestParameters.q?html}"/>
  </#if>
  <#if RequestParameters.subject??>
    <input type="hidden" name="subject" value="${RequestParameters.subject?html}"/>
  </#if>
  <#if RequestParameters.page?? >
    <input type="hidden" name="page" value="${RequestParameters.page?html}"/>
  </#if>
    <div class="filter-option date">
      <h5>Filter by date</h5>
      <select name="dateRange">
      <#list dateRanges as dateRange>
        <option value="${dateRange}" <#if (selectedDateRange == dateRange)>
                selected="selected"</#if>>${dateRange.description}</option>
      </#list>
      </select>
    </div>

    <div class="filter-option sort">
      <h5>Sort by</h5>
      <select name="sortOrder">
      <#list sortOrders as sortOrder>
        <option value="${sortOrder}" <#if (selectedSortOrder == sortOrder)>
                selected="selected"</#if>>${sortOrder.description}</option>
      </#list>
      </select>
    </div>

    <div class="filter-application">
      <button class="rounded cancel" type="reset">cancel</button>
      <button class="rounded apply" type="submit">apply</button>
    </div>
  </form>
</div><#--end filter-box-->

<div class="filter-container clearfix">
  <h3>
  ${searchResults.numFound} ${(searchResults.numFound == 1)?string("result", "results")} found
  <#if subjectName??>
    in ${subjectName}
  </#if>
  </h3>
  <button class="filter-button coloration-white-on-color">
    <span class="text">Filter & Sort</span>
    <span class="arrow">expand</span class="arrow">
  </button>
</div>

<div id="results-content" class="content">

<#if searchResults.numFound gt 0>
  <div id="display-options">
    <div class="buttongroup clearfix">
      <button data-type="full-citation">full citation</button>
      <button class="active" data-type="title-and-author">title &amp; author</button>
      <button data-type="title-only">title only</button>
    </div>
  </div>
</#if>

  <section id="article-items" class="title-and-author">

  <#list searchResults.docs as doc>
    <article class="article-item" data-article-id="${doc.id}">

    <#-- TODO: implement save to article list.  Not MVP.
    <a class="save-article circular coloration-text-color" data-list-type="multi">x</a>
    -->

    <#-- We rely here on the fact that search in wombat is always restricted to the current
         journal.  If this changes, we'll have to pass in the site in the href.  -->
      <a href="${doc.link}" class="article-title">${doc.title}</a>

      <p class="author-list">
        <#list doc.author_display![] as author>
          <a class="author-info" data-author-id="1">${author}</a><#if author_has_next>,</#if>
        </#list>
      </p>

      <p class="citation">
      ${doc.article_type}<br/>
        published <@formatJsonDate date="${doc.publication_date}" format="dd MMM yyyy" />
        | ${doc.journal_name[0]}<br/>
      ${doc.id}<br/>

        <#assign views = doc.counter_total_all!0 />
        <#assign citations = doc.alm_scopusCiteCount!0 />
        <#assign saves = doc.alm_citeulikeCount!0 + doc.alm_mendeleyCount!0 />
        <#assign shares = doc.alm_twitterCount!0 + doc.alm_facebookCount!0 />
        <a>Views: ${views}</a> |
        <a>Citations: ${(citations > 0)?string("Yes", "none")}</a> |
        <a>Saves: ${(saves > 0)?string("Yes", "none")}</a> |
        <a>Shares: ${(shares > 0)?string("Yes", "none")}</a>
      </p><#--end full citation-->

      <nav class="article-options-menu clearfix">
        <@siteLink handlerName="figuresPage" queryParameters={"id": doc.id} ; href>
          <a href="${href}">Figures</a>
        </@siteLink>
        <@siteLink handlerName="article" queryParameters={"id": doc.id} ; href>
          <a href="${href}#abstract">Abstract</a>
        </@siteLink>

      <#-- TODO: what does this link mean?  Do we need to expand all accordion sections?  -->
        <@siteLink handlerName="article" queryParameters={"id": doc.id} ; href>
          <a href="${href}">Full text</a>
        </@siteLink>
        <@siteLink handlerName="assetFile" queryParameters={"type": "printable", "id": doc.id} ; href>
          <a href="${href}">PDF</a> <#-- TODO: Not all articles have PDF files. May want to suppress if possible. -->
        </@siteLink>
      </nav><#--end article-options-menu-->

    </article>
  </#list>

  <#assign numPages = (searchResults.numFound / resultsPerPage)?ceiling />
  <#assign currentPage = (RequestParameters.page!1)?number />
  <#assign path = "" />
  <#include "../common/paging.ftl" />
  <@paging numPages currentPage path parameterMap />
  </section>
<#include "../common/bottomMenu/bottomMenu.ftl" />
</div><#--end content-->

<section id="article-info-window" class="modal-info-window">

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content">

  </div>

  <a class="modal-search coloration-white-on-color square-full">search for this author</a>

</section><#--end model info window-->

<div id="container-main-overlay"></div>
<@page_footer />

