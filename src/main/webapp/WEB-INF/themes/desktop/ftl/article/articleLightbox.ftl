<div id="figure-lightbox-container"></div>

<script id="figure-lightbox-template" type="text/template">
  <div id="figure-lightbox" class="reveal-modal full" data-reveal aria-hidden="true"
       role="dialog">
    <div class="lb-header">
      <h1 id="lb-title"><%= articleTitle %></h1>

      <div id="lb-authors">
        <% authorList.split(',').forEach(function (author) { %>
        <span><%= author.trim() %></span>
        <% }) %>
      </div>

      <ul class="lb-nav">
        <li class="abst">Abstract</li>
        <li class="figs tab-active">Figures</li>
        <li class="refs">References</li>
      </ul>

      <div class="lb-close" title="close">&nbsp;</div>
    </div>
    <div class="img-container">
      <div class="loader"> <i class="fa-spinner"></i> </div>
      <img class="main-lightbox-image" src=""/>
      <aside id="figures-list">
        <% figureList.each(function (ix, figure) { %>
        <div class="change-img" data-doi="<%= figure.getAttribute('data-doi') %>">
          <img class="aside-figure" src="<@siteLink handlerName="figureImage" queryParameters={"size": "inline"}/>&id=<%= figure.getAttribute('data-doi') %>" />
        </div>
        <% }) %>
        <div class="dummy-figure">
        </div>
      </aside>
    </div>
    <div id="lightbox-footer">
      <div id="btns-container" class="lightbox-row">
        <div class="range-slider-container">
          <div id="lb-zoom-min"></div>
          <div class="range-slider round" data-slider data-options="step: 0.05; start: 1; end: 5; initial: 1;">
            <span class="range-slider-handle" role="slider" tabindex="0"></span>
            <span class="range-slider-active-segment"></span>
            <input type="hidden">
          </div>
          <div id="lb-zoom-max"></div>
        </div>
        <div id="fig-btns-container">
          <span class="fig-btn all-fig-btn"><i class="icon icon-all"></i> All Figures</span>
          <span class="fig-btn next-fig-btn"><i class="icon icon-next"></i> Next</span>
          <span class="fig-btn prev-fig-btn"><i class="icon icon-prev"></i> Previous</span>
        </div>
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
          <span id="view-more">show more<i class="icon-arrow-right"></i></span>
        </p>
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
      <a href="<@siteLink handlerName="figureImage" queryParameters={"size": "original"}/>&id=<%= doi %>" title="original image">
        <span class="download-btn">TIFF</span>
      </a>
    </div>
    <div class="item">
      <a href="<@siteLink handlerName="figureImage" queryParameters={"size": "large"}/>&id=<%= doi %>" title="large image">
        <span class="download-btn">PNG</span>
      </a>
    </div>
    <div class="item">
      <a href="<@siteLink handlerName="powerPoint"/>?id=<%= doi %>" title="PowerPoint slide">
        <span class="download-btn">PPT</span>
      </a>
    </div>

  </div>
</script>