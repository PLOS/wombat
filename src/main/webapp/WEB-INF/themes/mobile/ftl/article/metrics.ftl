<#assign articleDoi = article.doi />

<#include "../common/headContent.ftl" />
<#include "../common/configJs.ftl" />

<div id="article-content" class="content">



<#include "metricsBody.ftl"/>

</div><#-- end home-content -->
<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>

<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>


<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js"/>
<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>


<@js src="resource/js/metrics.js"/>

<#include "../common/footContent.ftl" />

