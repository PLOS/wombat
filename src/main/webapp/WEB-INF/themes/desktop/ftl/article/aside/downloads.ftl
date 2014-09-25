
<#assign downloadPrefix = 'article/asset?id=' + article.doi  />
 <#-- uses jquery.menu-aim.js for the dropdown menu -->
<div class="dloads-container">
  <ul class="dload-menu" role="menu" >
    <li><a href="${downloadPrefix}.PDF" class="dload-pdf" id="downloadPdf">Download PDF</a></li>
    <li class="dload-hover" data-submenu-id="submenu-dloads"><a href="#" >&nbsp;</a></div>  <#--this is the row-->

    <ul id="submenu-dloads"class="dload-xml">
      <#include "citationLink.ftl" />
      <li><a href="${downloadPrefix}.XML" id="downloadXml">XML</a></li>
    </ul>

  </li>
</ul>

