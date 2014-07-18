<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#include "../common/head.ftl" />
<body class="home ${journalKey?lower_case}">

<#include "../common/header/header.ftl" />

<#include "body.ftl" />
<#include "cmsJS.ftl" />

<div class="spotlight">
<#include "adSlotBottom.ftl" />
</div>


<#include "../common/footer/footer.ftl" />

<#--This polyfill is so that IE8 can use rems. I don't think we can call this using the asset manager. -->

<!--the previous two scripts enable the use of foundation's dropdowns to work in IE8 -->
<@js src="resource/js/global.js" />

<@js src="resource/js/pages/home.js" />
<!--[if IE 8]>
<script src="resource/js/vendor/rem.min.js"></script>
<script src="resource/js/vendor/html5shiv.js"></script>
<script src="resource/js/vendor/respond.min.js"></script>
<![endif]-->


<@renderJs />
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="resource/js/components/blogfeed.js" /></script>

<script>
$(document).ready(function() {
  google.setOnLoadCallback(function(){
    feedLoaded();
  });
});
</script>


<!--TODO: the following need to be on all pages, not just home-->
</body>
</html>
