<#include 'default.ftl' />

<#macro page_footer_extra>
  <#include "../article/articleSuffix.ftl" />
  <#include "../common/bottomMenu/bottomMenu.ftl" />
  </div>
<#--end content-->
<section id="article-info-window" class="modal-info-window">

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content">

  </div>

  <a href="#" class="modal-search square-full coloration-white-on-color">search for this author</a>

</section><#--end modal info window-->

<div id="container-main-overlay"></div>
</#macro>

<#macro page_header_extra>
  <#include "../article/articlePrefix.ftl" />
</#macro>
