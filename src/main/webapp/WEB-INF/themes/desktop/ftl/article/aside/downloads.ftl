

<#assign articleURI = 'info:doi/' + article.doi  />
 <#assign downloadObject> ${legacyUrlPrefix}article/fetchObject.action?uri=info:doi/${article.doi}&representation=</#assign>
<!--[if IE 9]>
<style>
.dload-xml {margin-top: 38px}
</style>
<![endif]-->
<div class="dload-menu">
  <div class="dload-pdf"><a href="${downloadObject}PDF" id="downloadPdf" target="_blank">Download PDF</a></div>
  <div data-js-tooltip-hover="trigger" class="dload-hover">&nbsp;
    <ul class="dload-xml" data-js-tooltip-hover="target">
      <#include "citationLink.ftl" />
      <li><a href="article/asset?id=${article.doi}.XML" id="downloadXml">XML</a></li>
    </ul>

  </div>
</div>


