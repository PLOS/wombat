
<#assign downloadPrefix = 'article/asset?id=' + article.doi  />

<div class="dloads-container">
  <ul class="dload-menu">
    <li><a href="${downloadPrefix}.PDF" class="dload-pdf" id="downloadPdf">Download PDF</a></li>
    <li data-js-tooltip-hover="trigger"><a href="#" class="dload-hover">&nbsp;</a> 

      <ul class="dload-xml" data-js-tooltip-hover="target">
        <#include "citationLink.ftl" />
        <li><a href="${downloadPrefix}.XML" id="downloadXml">XML</a></li>
      </ul>

    </li>
  </ul>
</div>

