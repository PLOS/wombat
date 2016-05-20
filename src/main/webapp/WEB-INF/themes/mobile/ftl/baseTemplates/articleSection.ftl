<#include 'base.ftl' />

<#macro page_head cssFile='' title='' bodyId=''>
  <@base_page_head cssFile title bodyId />
  <#include "../common/bodyAnalytics.ftl" />

  <div id="container-main">
    <#include "../article/backToArticle.ftl" />
  <@page_head_extra />
</#macro>

<#macro page_footer>
  <@base_page_footer />
  <#include "../common/footer/footer.ftl" />

  </div>
  <!--end container-main-->

  <#include "../common/bodyJs.ftl" />
</body>
</html>
</#macro>