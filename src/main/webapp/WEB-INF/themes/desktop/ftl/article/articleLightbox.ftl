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

<div id="figure-lightbox-container"></div>

<script id="figure-lightbox-template" type="text/template">
  <div id="figure-lightbox" class="reveal-modal full" data-reveal aria-hidden="true"
       role="dialog">
    <div class="lb-header">
      <h1 id="lb-title"><%= articleTitle %></h1>

      <div id="lb-authors">
        <#assign maxFigureAuthorsToShow = 3 />
        <#if authors?size gt maxFigureAuthorsToShow + 1>
          <#list authors as author>
            <#if author_index lt (maxFigureAuthorsToShow - 1) >
              <span>${author.fullName}</span>
            </#if>
          </#list>
          <a class="more-authors" href="<@siteLink path="/article/authors?id=${article.doi}"
            title="show all authors"/>">...</a>
          <#assign authorLast = authors?last />
          <span>${authorLast.fullName}</span>
        <#else>
          <#list authors as author>
            <span>${author.fullName}</span>
          </#list>
        </#if>
      </div>

      <div class="lb-close" title="close">&nbsp;</div>
    </div>
    <div class="img-container">
      <div class="loader"> <i class="fa-spinner"></i> </div>
      <img class="main-lightbox-image" src=""/>
      <aside id="figures-list">
        <% figureList.each(function (ix, figure) { %>
        <div class="change-img" data-doi="<%= figure.getAttribute('data-doi') %>">
          <img class="aside-figure" src="<@siteLink handlerName="figureImage" queryParameters=(versionPtr + {"size": "inline"})/>&id=<%= figure.getAttribute('data-doi') %>" />
        </div>
        <% }) %>
        <div class="dummy-figure">
        </div>
      </aside>
    </div>
    <div id="lightbox-footer">

      <div id="btns-container" class="lightbox-row <% if(figureList.length <= 1) { print('one-figure-only') } %>">
        <div class="fig-btns-container reset-zoom-wrapper left">
          <span class="fig-btn reset-zoom-btn">Reset zoom</span>
        </div>
        <div class="zoom-slider-container">
          <div class="range-slider-container">
            <span id="lb-zoom-min"></span>
            <div class="range-slider round" data-slider data-options="start: 20; end: 200; initial: 20;">
              <span class="range-slider-handle" role="slider" tabindex="0"></span>
              <span class="range-slider-active-segment"></span>
              <input type="hidden">
            </div>
            <span id="lb-zoom-max"></span>
          </div>
        </div>
        <% if(figureList.length > 1) { %>
        <div class="fig-btns-container">
          <span class="fig-btn all-fig-btn"><i class="icon icon-all"></i> All Figures</span>
          <span class="fig-btn next-fig-btn"><i class="icon icon-next"></i> Next</span>
          <span class="fig-btn prev-fig-btn"><i class="icon icon-prev"></i> Previous</span>
        </div>
        <% } %>
      </div>
      <div id="image-context">
      </div>
    </div>
  </div>
</script>

<script id="image-context-template" type="text/template">
  <div class="footer-text">
    <div id="figure-description-wrapper">
      <div id="view-more-wrapper" style="<% descriptionExpanded? print('display:none;') : '' %>">
        <span id="figure-title"><%= title %></span>
        <p id="figure-description">
          <%= description %>&nbsp;&nbsp;
        </p>
        <span id="view-more">show more<i class="icon-arrow-right"></i></span>

      </div>
      <div id="view-less-wrapper" style="<% descriptionExpanded? print('display:inline-block;') : '' %>" >
        <span id="figure-title"><%= title %></span>
        <p id="full-figure-description">
          <%= description %>&nbsp;&nbsp;
          <span id="view-less">show less<i class="icon-arrow-left"></i></span>
        </p>
      </div>
    </div>
  </div>
  <div id="show-context-container">
    <a class="btn show-context" href="<%= showInContext(strippedDoi) %>">Show in Context</a>
  </div>
  <div id="download-buttons">
    <h3>Download:</h3>
    <div class="item">
      <a href="<@siteLink handlerName="figureImage" queryParameters=(versionPtr + {"size": "original", "download": ""})/>&id=<%= doi %>" title="original image">
        <span class="download-btn">TIFF</span>
      </a>
      <span class="file-size"><%= fileSizes.original %></span>
    </div>
    <div class="item">
      <a href="<@siteLink handlerName="figureImage" queryParameters=(versionPtr + {"size": "large", "download": ""})/>&id=<%= doi %>" title="large image">
        <span class="download-btn">PNG</span>
      </a>
      <span class="file-size"><%= fileSizes.large %></span>
    </div>
    <div class="item">
      <a href="<@siteLink handlerName="powerPoint"/>?id=<%= doi %>" title="PowerPoint slide">
        <span class="download-btn">PPT</span>
      </a>
    </div>

  </div>
</script>