<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Results" />
<#assign depth = 0 />
<#include "../common/head.ftl" />

<body id="page-results" class="plosone">
  
  <div id="container-main">

    <#include "../common/header.ftl" />

    <div id="filter-results-container" class="filter-box coloration-white-on-color" data-function="date-and-sort">
      <form id="sortAndFilterSearchResults" action="search" method="get">
        <input type="hidden" name="q" value="${RequestParameters.q}" />
        <div class="filter-option date">
          <h5>Filter by date</h5>
          <select name="dateRange">
            <#list dateRanges as dateRange>
              <option value="${dateRange}" <#if (selectedDateRange == dateRange)> selected="selected"</#if>>${dateRange.description}</option>
            </#list>
          </select>
        </div>

        <div class="filter-option sort">
          <h5>Sort by</h5>
          <select name="sortOrder">
          <#list sortOrders as sortOrder>
            <option value="${sortOrder}" <#if (selectedSortOrder == sortOrder)> selected="selected"</#if>>${sortOrder.description}</option>
          </#list>
          </select>
        </div>

        <div class="filter-application">
          <button class="rounded cancel" type="reset">cancel</button>
          <button class="rounded apply" type="submit">apply</button>
        </div>
      </form>
    </div>
    <!--end filter-box-->

    <div class="filter-container clearfix">
      <h3>${searchResults.numFound} ${(searchResults.numFound == 1)?string("result", "results")} found</h3>
      <button class="filter-button coloration-white-on-color">
        <span class="text">Filter & Sort</span>
        <span class="arrow">expand</span class="arrow">
      </button>
    </div>

    <div id="results-content" class="content">

      <div id="display-options">
        <div class="buttongroup clearfix">
          <button data-type="full-citation">full citation</button>
          <button class="active" data-type="title-and-author">title + author</button>
          <button data-type="title-only">title only</button>
        </div>
      </div>

      <section id="article-items" class="title-and-author">

        <#list searchResults.docs as doc>
          <article class="article-item" data-article-id="${doc.id}">
            <a class="save-article circular coloration-text-color" data-list-type="multi">x</a>
            <h2 class="article-title">${doc.title}</h2>

            <p class="author-list">
              <#list doc.author_display![] as author>
                <a class="author-info" data-author-id="1">${author}</a><#if author_has_next>,</#if>
              </#list>
            </p>

            <p class="citation">
              ${doc.article_type}<br/>
              published <@formatJsonDate date="${doc.publication_date}" format="dd MMM yyyy" />
              | ${doc.cross_published_journal_name[0]}<br/>
              ${doc.id}<br/>

              <#assign views = doc.counter_total_all!0 />
              <#assign citations = doc.alm_scopusCiteCount!0 />
              <#assign saves = doc.alm_citeulikeCount!0 + doc.alm_mendeleyCount!0 />
              <#assign shares = doc.alm_twitterCount!0 + doc.alm_facebookCount!0 />
              <a>Views: ${views}</a> |
              <a>Citations: ${(citations > 0)?string("Yes", "none")}</a> |
              <a>Saves: ${(saves > 0)?string("Yes", "none")}</a> |
              <a>Shares: ${(shares > 0)?string("Yes", "none")}</a>
            </p>
            <!--end full citation-->

            <nav class="article-options-menu clearfix">
              <a>Figures</a>
              <a>Abstract</a>
              <a>Full text</a>
              <a>PDF</a>
            </nav>
            <!--end article-options-menu-->

          </article>
        </#list>

        <#-- Search results paging.  This is the basic macro that displays a series of numbered page links.
             We use various combinations of it and ellipses below.  -->
        <#macro pageLinkRange first last selected>
          <#list first..last as i>
            <#assign linkClass = (i == selected)?string("number seq active text-color", "number seq text-color") />
            <#if selected == i >

            <#-- TODO: this should really be a span, not an a, but that messes up the styling right now. -->
              <a class="${linkClass}" data-page="${i}">${i}</a>
            <#else>
              <a href="search?<@replaceParams params=RequestParameters name="page" value=i />" class="${linkClass}" data-page="${i}">${i}</a>
            </#if>
          </#list>
        </#macro>

        <#assign numPages = (searchResults.numFound / resultsPerPage)?ceiling />
        <#assign currentPage = (RequestParameters.page!1)?number />
        <#if numPages gt 1>
          <nav id="article-pagination" class="nav-pagination">
            <#if currentPage gt 1>
              <a href="search?<@replaceParams params=RequestParameters name="page" value=currentPage - 1 />" class="previous switch">Previous Page</a>
            </#if>
            <#if numPages lt 10>
              <@pageLinkRange first=1 last=numPages selected=currentPage />
            <#elseif currentPage lt 4>
              <@pageLinkRange first=1 last=4 selected=currentPage />
              <span class="skip">...</span>
              <@pageLinkRange first=numPages last=numPages selected=currentPage />
            <#else>
              <@pageLinkRange first=1 last=1 selected=currentPage />
              <span class="skip">...</span>
              <#if currentPage lt numPages - 4>
                <@pageLinkRange first=currentPage - 1 last=currentPage + 1 selected=currentPage />
                <span class="skip">...</span>
                <@pageLinkRange first=numPages - 1 last=numPages selected=currentPage />
              <#else>
                <@pageLinkRange first=currentPage - 1 last=numPages selected=currentPage />
              </#if>
            </#if>
            <#if currentPage lt numPages>
              <a href="search?<@replaceParams params=RequestParameters name="page" value=currentPage + 1 />" class="next switch">Next Page</a>
            </#if>
          </nav>
        </#if>
      </section>
      <#include "../common/bottomMenu/bottomMenu.ftl" />
    </div>
    <!--end content-->

    <#include "../common/footer/footer.ftl" />
    
    <section id="article-info-window" class="modal-info-window">
      
      <div class="modal-header clearfix">
        <a class="close coloration-text-color">v</a>
      </div>

      <div class="modal-content">
        
      </div>

      <a class="modal-search coloration-white-on-color square-full">search for this author</a>

    </section>
    <!--end model info window-->

    <div id="container-main-overlay"></div>
  
  </div>
  <!--end container main-->

  <#include "../common/fullMenu/fullMenu.ftl" />
  <#include "../common/bodyJs.ftl" />
</body>
</html>
