<#include "../baseTemplates/articleSection.ftl" />

<#macro page_content>
<div id="content" class="related-content">
  <article>
    <#include "relatedContentBody.ftl" />
  </article>
</div>

  <#include "articleData.ftl" />
  <#include "../common/almQueryJs.ftl" />

  <@js src="resource/js/vendor/moment.js"/>
  <@js src="resource/js/pages/related_content.js"/>
</#macro>

<@render_page 'related-content.css' article.title />