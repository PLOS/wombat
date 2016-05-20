<#assign articleDoi = article.doi />
<#include "../baseTemplates/articleSection.ftl" />
<#assign title = article.title />
<#assign bodyId = 'page-authors' />

<@page_header />
<#include "metricsBody.ftl"/>

<#include "../common/configJs.ftl" />
<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>
<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>
<#include "articleData.ftl" />
<#include "../common/article/metricsJs.ftl" />
<@page_footer />
