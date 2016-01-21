<#if article.figures?has_content>
<div>
  <#list article.figures as figure>
    <#list figure?keys as key>
    ${key}
    </#list>
  <#-- Omit figures that don't appear in article body (e.g. a striking image) -->
    <#if figure.contextElement?has_content>

      <@siteLink path=("article/figure/image?size=small&id=" + figure.doi) ; src>
        <a href="#" class="lightbox-figure"
           data-figure-doi="${figure.doi?html}"
           data-figure-title="${figure.title?html}"
           data-figure-description="${figure.description?html}">
          <img src="${src?html}"
            <#if figure.title?has_content >
               alt="${figure.title?html}"
            </#if>
              />
        </a>
      </@siteLink>

    </#if>
  </#list>
</div>
<div id="figure-lightbox-container"></div>
</#if>

<script id="figure-lightbox-template" type="text/template">
  <div id="figure-lightbox" class="reveal-modal full" data-reveal aria-labelledby="modalTitle" aria-hidden="true"
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
      <img src=""/>
    </div>
    <div class="btns-container lightbox-row">
      <div class="range-slider-container">
        <div id="lb-zoom-min"></div>
        <div class="range-slider round" data-slider data-options="step: 0.05; start: 0.05; end: 5; initial: 1;">
          <span class="range-slider-handle" role="slider" tabindex="0"></span>
          <span class="range-slider-active-segment"></span>
          <input type="hidden">
        </div>
        <div id="lb-zoom-max"></div>
      </div>
      <div class="fig-btns-container">
        <span class="fig-btn all-fig-btn"><i class="icon icon-all"></i> All Figures</span>
        <span class="fig-btn next-fig-btn"><i class="icon icon-next"></i> Next</span>
        <span class="fig-btn prev-fig-btn"><i class="icon icon-prev"></i> Prev</span>
      </div>
    </div>
    <div class="lightbox-footer">
      <div class="footer-text">
        <span class="figure-title"><%= title %></span>
        <%= description %>
      </div>
      <div class="show-context-container">
        <a class="btn show-context" href="#">Show in Context</a>
      </div>
      <div class="download-buttons">
        <h3>Download:</h3>
        <div class="item">
          <a href="" title="PowerPoint slide">
            <span class="download-btn">PPT</span>
          </a>
        </div>
        <div class="item">
          <a href="" title="large image">
            <span class="download-btn">PNG</span>
          </a>
        </div>
        <div class="item">
          <a href="" title="original image">
            <span class="download-btn">TIFF</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</script>
