<#include "../common/htmlTag.ftl" />

<#assign title = '' />
<#assign depth = 0 />
<#include "../common/head.ftl" />
<@cssLink target="resource/css/issues.css" />
<@renderCssLinks />

<body>
<div id="container-main">

<#include "../common/header/headerContainer.ftl" />

<div id="article-content" class="content">

<#include "issuesBody.ftl"/>

</div><#-- end home-content -->

<#include "../common/footContent.ftl" />

