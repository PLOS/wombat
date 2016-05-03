<html>
<#assign title = "" /> <#-- use default -->
<#assign cssFile="site-content.css"/>
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<article>
  <#include "citationDownloadBody.ftl" />
</article>

<#include "../common/footer/footer.ftl" />
</body>
</html>
