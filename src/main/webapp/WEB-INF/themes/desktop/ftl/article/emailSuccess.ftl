<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign cssFile = 'site-content.css'/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<#include "emailSuccessBody.ftl" />
<#include "../common/footer/footer.ftl" />

<@renderJs />

</body>
</html>
