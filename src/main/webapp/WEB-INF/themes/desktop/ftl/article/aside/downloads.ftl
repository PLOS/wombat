
<#assign downloadPrefix = 'article/asset?id=' + article.doi  />

<#--<div class="dloads-container"></div>-->
  <div class="dload-menu">
    <div class="dload-pdf"><a href="${downloadPrefix}.PDF" id="downloadPdf">Download PDF</a></div>
    <div data-js-tooltip-hover="trigger" class="dload-hover">&nbsp;

      <ul class="dload-xml" data-js-tooltip-hover="target">
        <#include "citationLink.ftl" />
        <li><a href="${downloadPrefix}.XML" id="downloadXml">XML</a></li>
      </ul>

    </div>
  </div>


