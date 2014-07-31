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
<#include "../common/bodyBottomJs.ftl" />

</body>
</html>
