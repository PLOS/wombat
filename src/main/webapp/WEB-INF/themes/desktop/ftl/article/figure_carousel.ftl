<#if article.figures?has_content>
<div id="figure-carousel">
  <#list article.figures as figure>

  <#--
      TODO: In JavaScript, set href to anchor of the inline figure

      The legacy implementation relied on PLOS typesetting conventions in order to infer the figure ID from its DOI.
      Instead, we should have JavaScript match the figure's DOI (which we know right now) to the figure element's ID
      (which is defined in XML, so we must find it by inspecting the transformed HTML).
    -->
    <a href="#"
       data-doi="${figure.doi}">
      <@siteLink path=("article/figure/image?size=inline&amp;id=" + figure.doi) ; src>
        <img src="${src}" alt=""/>
      </@siteLink>
    </a>
  </#list>
</div>
</#if>
