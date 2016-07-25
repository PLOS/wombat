<!--[if IE 9]>
<style>
.dload-xml {margin-top: 38px}
</style>
<![endif]-->
<div class="dload-menu">
<#if articleItems[article.doi].files?keys?seq_contains("printable")>
  <div class="dload-pdf">
    <a href="<@siteLink handlerName="assetFile" queryParameters={"type": "printable", "id": article.doi} />"
       id="downloadPdf" target="_blank">Download PDF</a>
  </div>
<#else>
  <div class="dload-pdf no-pdf">Download</div>
</#if>
  <div data-js-tooltip-hover="trigger" class="dload-hover">&nbsp;
    <ul class="dload-xml" data-js-tooltip-hover="target">
      <li><a href="<@siteLink handlerName="citationDownloadPage" queryParameters={'id': article.doi} />"
             id="downloadCitation">Citation</a></li>
      <li><a href="<@siteLink handlerName="assetFile" queryParameters={"type": "manuscript", "id": article.doi} />"
             id="downloadXml">XML</a>
      </li>
    </ul>

  </div>
</div>
