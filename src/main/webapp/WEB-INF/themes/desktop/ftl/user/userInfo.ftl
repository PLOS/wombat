<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = "" /> <#-- use default -->
<#assign cssFile = 'site-content.css'/>
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<#include "userInfoBody.ftl" />

<#include "../common/footer/footer.ftl" />
<@renderJs />
</body>
</html>
