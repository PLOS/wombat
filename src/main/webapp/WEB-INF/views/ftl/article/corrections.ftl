<#include "../common/htmlTag.ftl" />

<#assign title = "PLOS - Corrections" />
<#assign depth = 1 />
<#include "../common/head.ftl" />

<body id="page-comments">
<div id="container-main">
<#include "backToArticle.ftl" />

  <div id="comment-content" class="content">
    <h3 class="comments-header">Corrections(${articleCorrections?size})</h3>
  <#assign articleComments = articleCorrections />
  <#assign mode = "corrections" />
  <#include "commentsBody.ftl" />
  </div><#--end content-->

<#include "../common/footer/footer.ftl" />

</div><#--end container main-->

<#include "../common/bodyJs.ftl" />
</body>
</html>
