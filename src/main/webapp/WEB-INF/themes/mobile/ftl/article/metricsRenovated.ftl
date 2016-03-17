<#assign articleDoi = article.doi />

<#include "../common/htmlTag.ftl" />

<#assign title = article.title />
<#include "../common/head.ftl" />
<#include "../common/configJs.ftl" />
<#include "backToArticle.ftl" />
<div id="container-main" class="metrics">



<#include "metricsBody.ftl"/>

</div><#-- end home-content -->
<@js src="resource/js/vendor/underscore-min.js"/>
<@js src="resource/js/vendor/q.min.js"/>

<@js src="resource/js/util/alm_config.js"/>


<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>


<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js"/>
<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>


<@js src="resource/js/util/class.js"/>
<@js src="resource/js/util/alm_query_promise.js"/>
<@js src="resource/js/util/article_data.js"/>
<@js src="resource/js/util/number.js"/>
<@js src="resource/js/components/metric_tile.js"/>
<@js src="resource/js/components/metrics_tab_component.js"/>
<@js src="resource/js/components/discussed_section.js"/>
<@js src="resource/js/components/viewed_section.js"/>
<@js src="resource/js/pages/metrics_tab.js"/>

<#include "../common/footContent.ftl" />

