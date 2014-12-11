<#if article.figures?has_content>
<#-- Hidden in its original position. Will be shown by figure_carousel.js after it is moved. -->
<div id="figure-carousel-section" style="display: none">
  <h2>Figures</h2>

  <div id="figure-carousel">

    <div class="carousel-wrapper"><#-- Horizontally scrolling view window -->
      <div class="slider"><#-- Large-width container -->
        <#list article.figures as figure>

          <div class="carousel-item" data-doi="${figure.doi}">

              <@siteLink path=("article/figure/image?size=inline&id=" + figure.doi) ; src>
                <img src="${src?html}"
                     <#if figure.title?has_content >
                     alt="${figure.title?html}"
                     </#if>
                />
              </@siteLink>

          </div>

        </#list>
      </div>
    </div>

    <div class="carousel-control">
      <span class="button previous"></span>
      <span class="button next"></span>
    </div>
    <div class="carousel-page-buttons">

    </div>
  </div>
</div>
<@js src="resource/js/vendor/jquery.touchswipe.js" />
<@js src="resource/js/components/figure_carousel.js" />
</#if>

