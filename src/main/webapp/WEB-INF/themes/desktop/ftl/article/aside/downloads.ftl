<!--[if IE 9]>
<style>
.dload-xml {margin-top: 38px}
</style>
<![endif]-->
<div class="dload-menu">
  <div class="dload-pdf">
    <a href="<@siteLink handlerName="asset" queryParameters={"id": article.doi + ".PDF"} />"
       id="downloadPdf" target="_blank">Download PDF</a>
  </div>
  <div data-js-tooltip-hover="trigger" class="dload-hover">&nbsp;
    <ul class="dload-xml" data-js-tooltip-hover="target">
      <li><a href="<@siteLink handlerName="citationDownloadPage" queryParameters={'id': article.doi} />"
             id="downloadCitation">Citation
      </a>
      </li>
      <li><a href="<@siteLink handlerName="asset" queryParameters={"id": article.doi + ".XML"} />"
             id="downloadXml">XML</a>
      </li>
    </ul>

  </div>
</div>
