<!DOCTYPE html>
<!--[if lt IE 7]><html class="no-js lt-ie9 lt-ie8 lt-ie7"><![endif]-->
<!--[if IE 7]><html class="no-js lt-ie9 lt-ie8"><![endif]-->
<!--[if IE 8]><html class="no-js lt-ie9"><![endif]-->
<!--[if IE 9]><html class="no-js ie9"><![endif]-->
<!--[if gt IE 9]><html class="no-js"><!--<![endif]-->

<#assign title = article.title />
<#assign depth = 0 />
<#include "../common/head.ftl" />

<body>

<#include "articlePrefix.ftl" />

<div id="articleText">
${articleText}
</div>

<#include "articleSuffix.ftl" />

</body>
</html>
