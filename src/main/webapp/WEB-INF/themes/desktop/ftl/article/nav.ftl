<div id="nav-article">
  <ul class="nav-secondary">

    <li class="nav-comments" id="nav-comments">
      <a href="article/comments?id=${article.doi}">Reader Comments (${commentCount.root})</a>
    </li>

    <li class="nav-media" id="nav-media" data-doi="${article.doi}">
      <a href="<@siteLink handlerName="articleRelatedContent" queryParameters={"id": article.doi} />">
        Media Coverage <span id="media-coverage-count"></span>
      </a>
    </li>

  <#if figures?has_content>
    <li id="nav-figures"><a href="#" data-doi="${article.doi}">Figures</a></li>
  </#if>
  </ul>
</div>
<@js src="resource/js/components/scroll.js"/>
<@js src="resource/js/components/nav_builder.js"/>
<@js src="resource/js/components/floating_nav.js"/>
