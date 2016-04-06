<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign adPage="Homepage"/>
<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="home ${journalStyle}">

<div id="test"></div>

<#include "../common/header/headerContainer.ftl" />

<#include "body.ftl" />
<#include "cmsJS.ftl" />

<div class="spotlight">
<#include "adSlotBottom.ftl" />
</div>

<#include "../common/footer/footer.ftl" />
<@js src="resource/js/vendor/moment.js" />
<@js src="resource/js/components/blogfeed.js" />

<@js src="resource/js/pages/home.js" />

<@renderJs />


</body>
</html>
