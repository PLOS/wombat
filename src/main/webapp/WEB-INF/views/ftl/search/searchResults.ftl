<!DOCTYPE html>
<!--[if lt IE 7]>    <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>     <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>     <html class="no-js lt-ie9"> <![endif]-->
<!--[if IE 9]>     <html class="no-js ie9"> <![endif]-->
<!--[if gt IE 9]><!--> <html class="no-js"> <!--<![endif]-->

<#assign title = "PLOS - Results" />
<#assign depth = 0 />
<#include "../common/head.ftl" />

<body id="page-results" class="plosone">
  
  <div id="container-main">

    <header id="common-header">
      <div class="search-expanded">
        <div class="search-form-container coloration-bg">
            <input id="search-input" type="text" class="search-field" placeholder="Search articles...">
            <div class="search-buttons">
                <button id="search-cancel" class="rounded">cancel</button>
                <button id="search-execute" class="rounded">search</button>
            </div>
        </div>
      </div>
      <div id="site-header-container" class="coloration-border-top">
          
              <a id="site-menu-button">Site Menu</a>
              <a class="site-search-button color-active"><span class="icon">Search</span></a>
              <a><h1 id="site-logo">PLOS</h1></a>
          
      </div>
      <nav id="article-menu" class="menu-bar">
          <ul>
              <li>
                  <a id="menu-browse">Browse Topics</a>
              </li>
              <li>
                  <a id="menu-saved">Saved Items</a>
              </li>
              <li>
                  <a id="menu-recent">Recent History</a>
              </li>
          </ul>
      </nav>
    </header>
    <!--end common-header-->

    <div id="filter-results-container" class="filter-box coloration-white-on-color" data-function="date-and-sort">
      <div class="filter-option date">
        <h5>Filter by date</h5>
        <select>
          <option value="all-time">All Time</option>
          <option value="option-2">Option 2</option>
          <option value="option-3">Option 3</option>
        </select>
      </div>

      <div class="filter-option sort">
        <h5>Sort by</h5>
        <select>
          <option value="relevance">Relevance</option>
          <option value="option-2">Option 2</option>
          <option value="option-3">Option 3</option>
        </select>
      </div>

      <div class="filter-application">
        <button class="rounded cancel">cancel</button>
        <button class="rounded apply">apply</button>
      </div>

    </div>
    <!--end filter-box-->

    <div class="filter-container clearfix">
      <h3>${searchResults.numFound} results found</h3>
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
              <#list doc.author_display as author>
                <a class="author-info" data-author-id="1">${author}</a><#if author_has_next>,</#if>
              </#list>
            </p>

            <p class="citation">
              ${doc.article_type}<br/>
              published <@formatJsonDate date="${doc.publication_date}" format="dd MMM yyyy" />
              | ${doc.cross_published_journal_name[0]}<br/>
              ${doc.id}<br/>

              <#-- TODO  -->
              <a>Views: 3,233</a> | <a>Citations: Yes</a> | <a>Bookmarks: None</a>
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
      </section>

      <nav class="article-menu-bottom small">
        <a class="btn-lg">PLOS Journals</a>
        <a class="btn-lg med">PLOS Blogs</a>
        <div class="btn-top-container">
          <span class="btn-text">Back to Top</span>
          <a class="btn">Back to Top</a>
        </div>
      </nav>

    </div>
    <!--end content-->

    <footer id="common-footer" class="footer">
      <nav class="footer-menu">
        <ul>
          <li>
            <a class="coloration-light-text">About Us</a>
          </li>
          <li>
            <a class="coloration-light-text">Full Site</a>
          </li>
          <li>
            <a class="coloration-light-text">Feedback</a>
          </li>
        </ul>
      </nav>

      <p class="footer-credits">
        <a class="bold">Ambra 2.4.2</a> Managed Colocation provided by <br/><a class="bold">Internet Systems Consortium.</a>
      </p>

      <nav class="footer-secondary-menu">
        <ul>
          <li>
            <a>Privacy Policy</a>
          </li>
          <li>
            <a>Terms of Use</a>
          </li>
          <li>
            <a>Advertise</a>
          </li>
          <li>
            <a>Media Inquiries</a>
          </li>
        </ul>
      </nav>

    </footer>
    
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


  
  <div id="common-menu-container" class="full-menu-container coloration-border-top">
    <nav class="full-menu">
      <ul class="primary accordion">
        <li class="accordion-item">
          <a class="expander">
            <span class="arrow">Expand</span>
            For Authors
          </a>
          <ul class="secondary accordion-content">
            <li>
              <a class="btn-lg">Why Publish with PLOS ONE</a>
            </li>
            <li>
              <a class="btn-lg">Publication Criteria</a>
            </li>
            <li>
              <a class="btn-lg">Editorial Policies</a>
            </li>
            <li>
              <a class="btn-lg">Manuscript Guidelines</a>
            </li>
            <li>
              <a class="btn-lg">Figure and Table Guidelines</a>
            </li>
            <li>
              <a class="btn-lg">Supporting Information<br/>Guidelines lorem ipsum</a>
            </li>
            <li>
              <a class="btn-lg">Submitting a Manuscript</a>
            </li>
          </ul>
        </li>
        <li class="accordion-item">
          <a class="expander">
            <span class="arrow">Expand</span>
            About Us
          </a>
          <p class="accordion-content secondary">[TEMP - CONTENT]</p>
        </li>
        <li class="accordion-item">
          <a class="expander">
            <span class="arrow">Expand</span>
            Create an Account
          </a>
          <p class="accordion-content secondary">[TEMP - CONTENT]</p>
        </li>
        <li class="accordion-item">
          <a class="expander">
            <span class="arrow">Expand</span>
            Sign In
          </a>
          <p class="accordion-content secondary">[TEMP - CONTENT]</p>
        </li>
      </ul>
    </nav> 

    <div id="submit-manuscript-container" class="full-menu-tout">
      <h4 class="coloration-light-text">Submit Your Manuscript</h4>
      <ul class="std">
        <li>Fair, rigorous peer review</li>
        <li>Broad scope and wide reach</li>
      </ul>
      <div><a class="rounded coloration-white-on-color">get started</a></div>
    </div>
  </div>
  <!--end full menu container-->

  <#include "../common/bodyJs.ftl" />
</body>
</html>
