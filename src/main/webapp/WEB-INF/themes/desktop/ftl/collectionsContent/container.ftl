<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign cssFile="site-content.css"/>

<#include "head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/header.ftl" />

<#if collectionsData.content??>
  ${collectionsData.content}
</#if>

<div data-service="ember" data-${collectionsData.name}-container>
    LEMURS!!!!
</div>

<#include "../common/footer/footer.ftl" />


<@renderJs />

<#list collectionsData.js_sources as js_source>
<script src="<@siteLink path='indirect/'/>${js_source}" type="text/javascript"></script>
</#list>

</body>
</html>
