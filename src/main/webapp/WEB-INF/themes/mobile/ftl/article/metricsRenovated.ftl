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
<@js src="resource/js/vendor/underscore.string.min.js"/>
<@js src="resource/js/vendor/q.min.js"/>

<@js src="resource/js/util/alm_config.js"/>


<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>

<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>


<#include "../common/article/metricsJs.ftl" />

<#include "../common/footContent.ftl" />

