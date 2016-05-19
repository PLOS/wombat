<#include "../common/htmlTag.ftl" />

<#assign title = article.title />
<#include "../common/head.ftl" />

<body>

<#include "articlePrefix.ftl" />

<div id="articleText">
${articleText}
</div>

<#include "articleSuffix.ftl" />

</body>
</html>
