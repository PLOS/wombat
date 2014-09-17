<#include "../articleLinks.ftl" />
<#assign downloadPrefix = legacyUrlPrefix + 'article/' + articleId  />
<#-- TODO: add links below. needs backend -->
<div class="dloads-container">
  <div class="dload-pilly">
    <a href="" class="dload-pdf" id="downloadPdf" target="_blank">Download PDF</a>
    <div class="dload-hover">&nbsp;</div>
  </div>
  <div class="dload-xml">
    <ul>
      <li><a href="" id="downloadCitation">Citation</a></li>
      <li><a href="" id="downloadXml">XML</a></li>
    </ul>
  </div>
</div>

