<div id="nav-article">
  <ul>
    <li class="nav-col-comments"><a href="comments?id=${article.doi}">Reader Comments (${articleComments?size})</a></li>
    <li id="nav-figures"><a data-doi="${article.doi}">Figures</a></li><#-- TODO: Wire to figure lightbox -->
  </ul>
</div>
<@js src="resource/js/components/nav_builder.js"/>
