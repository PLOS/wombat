<#if article.figures?has_content>
<div id="figure-carousel-section">
  <h3>Figures</h3>

  <div id="figure-carousel">

    <div class="carousel-wrapper">
      <div class="slider">
        <#list article.figures as figure>

          <div class="carousel-item" data-doi="${figure.doi}">
            <a>
            <#--
              JavaScript will set the href, by finding the div.figure whose data-doi attr matches this one.
              The figure ID is defined in the XML, and we don't know it in this context,
              but can find it by inspecting the transformed HTML below here.
              -->

              <@siteLink path=("article/figure/image?size=inline&amp;id=" + figure.doi) ; src>
                <img src="${src}" alt=""/>
              </@siteLink>
            </a>
          </div>
        </#list>
      </div>

    </div>
  </div>
</div>
</#if>
<@js src="resource/js/components/figure_carousel.js" />
