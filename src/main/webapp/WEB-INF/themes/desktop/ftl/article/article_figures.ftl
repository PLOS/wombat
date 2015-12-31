<#if article.figures?has_content>
<div>
  <#list article.figures as figure>

  <#-- Omit figures that don't appear in article body (e.g. a striking image) -->
    <#if figure.contextElement?has_content>

      <@siteLink path=("article/figure/image?size=small&id=" + figure.doi) ; src>
        <a href="#" class="lightbox-figure" data-figure-doi="${figure.doi}">
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
      <h1 id="lb-title"></h1>

      <div id="lb-authors"></div>

      <ul class="lb-nav">
        <li class="abst">Abstract</li>
        <li class="figs">Figures</li>
        <li class="refs">References</li>
      </ul>

      <div class="lb-close" title="close">&nbsp;</div>
    </div>
    <div class="img-container">
      <img src=""/>
    </div>
    <div class="btns-container lightbox-row">
      <div class="lightbox-col-4">&nbsp;</div>
      <div class="range-slider-container lightbox-col-4">
        <div id="lb-zoom-min"></div>
        <div class="range-slider round" data-slider data-options="step: 0.05; start: 0.05; end: 5; initial: 1;">
          <span class="range-slider-handle" role="slider" tabindex="0"></span>
          <span class="range-slider-active-segment"></span>
          <input type="hidden">
        </div>
        <div id="lb-zoom-max"></div>
      </div>
    </div>
  </div>
</script>
