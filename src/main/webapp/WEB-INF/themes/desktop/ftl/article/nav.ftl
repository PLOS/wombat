<div id="nav-article">
  <ul class="nav-secondary">

    <li class="nav-comments" id="nav-comments">
      <a href="article/comments?id=${article.doi}">Reader Comments (${articleComments?size})</a>
    </li>

  <#macro mediaCoverageLink href>
    <li class="nav-media" id="nav-media" data-doi="${article.doi}">
      <a href="${href}">
        Media Coverage <span id="media-coverage-count"></span>
      </a>
    </li>
  </#macro>
  <#include "mediaCoverageLink.ftl" />

  <#if article.figures?? && article.figures?size &gt; 0 >
    <li id="nav-figures"><a href="#" data-doi="${article.doi}">Figures</a></li>
  </#if>
  </ul>
</div>
<@js src="resource/js/components/nav_builder.js"/>
<@js src="resource/js/components/floating_nav.js"/>
