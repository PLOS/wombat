<#include "../baseTemplates/articleSection.ftl" />
<#assign mainclass = "related-content" />
<#assign mainId = "content" />

<#assign title = article.title />
<#assign cssFile = 'related-content.css' />

<@page_header />
<article>
<#include "relatedContentBody.ftl" />
</article>

<#include "articleData.ftl" />
<#include "../common/almQueryJs.ftl" />

<@js src="resource/js/pages/related_content.js"/>
<@page_footer />