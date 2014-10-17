<#if article.figures?has_content>
<div id="figure-carousel">
  <#list article.figures as figure>

    <a class="carousel-item" data-doi="${figure.doi}">
    <#--
      JavaScript will set the href, by finding the div.figure whose data-doi attr matches this one.
      The figure ID is defined in the XML, and we don't know it in this context,
      but can find it by inspecting the transformed HTML below here.
      -->

      <@siteLink path=("article/figure/image?size=inline&amp;id=" + figure.doi) ; src>
        <img src="${src}" alt=""/>
      </@siteLink>
    </a>
  </#list>
</div>
</#if>
<@js src="resource/js/components/figure_carousel.js" />
