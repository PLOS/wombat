<#if article.figures?has_content>
<div id="figure-carousel-section">
  <h2>Figures</h2>

  <div id="figure-carousel">

    <div class="carousel-wrapper"><#-- Horizontally scrolling view window -->
      <div class="slider"><#-- Large-width container -->
        <#list article.figures as figure>

          <div class="carousel-item" data-doi="${figure.doi}">
            <#--<a title="${figure.title?html}">-->

            <#--&lt;#&ndash;-->

            <#-- When we implement the figure viewer: -->
              <#--JavaScript will set the href, by finding the div.figure whose data-doi attr matches this one.-->
              <#--The figure ID is defined in the XML, and we don't know it in this context,-->

              <#--but can find it by inspecting the transformed HTML below here.-->
              <#--&ndash;&gt;-->

              <@siteLink path=("article/figure/image?size=inline&id=" + figure.doi) ; src>
                <img src="${src?html}"
                     <#if figure.title?has_content >
                     alt="${figure.title?html}"
                     </#if>
                />
              </@siteLink>
            <#--</a>-->
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

