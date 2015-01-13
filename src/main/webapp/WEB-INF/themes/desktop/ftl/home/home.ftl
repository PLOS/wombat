<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="home ${journalStyle}">

<#include "../common/header/header.ftl" />

<#include "body.ftl" />
<#include "cmsJS.ftl" />

<div class="spotlight">
<#include "adSlotBottom.ftl" />
</div>

<#include "../common/footer/footer.ftl" />

<@js src="resource/js/pages/home.js" />
<@js src="resource/js/components/dateparse.js" />

<@renderJs />

<script type="text/javascript" src="https://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22feeds%22%2C%22version%22%3A%221.0%22%2C%22language%22%3A%22en%22%7D%5D%7D"></script>
<script src="<@siteLink path="resource/js/components/dateparse.js"/>"></script>
<script src="<@siteLink path="resource/js/components/blogfeed.js"/>"></script>

</body>
</html>
