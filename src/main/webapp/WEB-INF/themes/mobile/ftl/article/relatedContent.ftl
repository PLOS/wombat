<#include "../baseTemplates/articleSection.ftl" />

<#assign title = article.title />
<#assign cssFile = 'related-content.css' />

<@page_header />
<div id="content" class="related-content">
  <article>
    <#include "relatedContentBody.ftl" />
  </article>
</div>

<#include "articleData.ftl" />
<#include "../common/almQueryJs.ftl" />

<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/pages/related_content.js"/>
<@page_footer />