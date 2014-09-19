<#assign downloadPrefix = 'article/asset?id=' + article.doi  />

<div class="dloads-container" data-js-tooltip-hover="trigger" >
  <div class="dload-pilly">
    <a href="${downloadPrefix}.PDF" class="dload-pdf" id="downloadPdf">Download PDF</a>
    <div class="dload-hover">&nbsp;</div>
  </div>
  <div class="dload-xml">
    <ul>
      <li><#include "citationLink.ftl" /></li>
      <li><a href="${downloadPrefix}.XML" id="downloadXml">XML</a></li>
    </ul>
  </div>
</div>
