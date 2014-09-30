<div id="nav-article">
  <ul class="nav-secondary">
    <li class="nav-comments" id="nav-comments"><a href="comments?id=${article.doi}">Reader Comments (${articleComments?size})</a></li>
  <#if article.figures?? && article.figures?size &gt; 0 >
    <li id="nav-figures"><a data-doi="${article.doi}">Figures</a></li><#-- TODO: Wire to figure lightbox -->
  </#if>
  </ul>
</div>
<@js src="resource/js/components/nav_builder.js"/>
<@js src="resource/js/components/floating_nav.js"/>
