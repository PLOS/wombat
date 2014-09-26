
<#assign downloadPrefix = 'article/asset?id=' + article.doi  />
 <#-- uses jquery.menu-aim.js for the dropdown menu -->
<div class="dloads-container">
  <ul class="dload-menu" role="menu">
    <li><a href="${downloadPrefix}.PDF" class="dload-pdf" id="downloadPdf">Download PDF</a></li>
    <li data-submenu-id="submenu-dloads"><a href="#" class="dload-hover">&nbsp;</a>  <#--this is the row-->

      <ul class="dload-xml" id="submenu-dloads">
        <#include "citationLink.ftl" />
        <li><a href="${downloadPrefix}.XML" id="downloadXml">XML</a></li>
      </ul>

    </li>
  </ul>
</div>

