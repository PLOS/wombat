<#include "../common/htmlTag.ftl" />

<#assign title = '' />
<#assign depth = 0 />
<#include "../common/head.ftl" />

<body>
<div id="container-main">

<#include "../common/header/headerContainer.ftl" />

  <div id="article-contetn" class="content">

  <#include "browseVolumesBody.ftl"/>




  </div><#-- end home-content -->

<#include "../common/footer/footer.ftl" />

</div><#-- end container-main -->

<#include "../common/siteMenu/siteMenu.ftl" />
<#include "../common/bodyJs.ftl" />

</body>
</html>
