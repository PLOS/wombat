<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<!--[if IE 9]>
<style>
.dload-xml {margin-top: 38px}
</style>
<![endif]-->
<div class="dload-menu">
<#if articleItems[article.doi].files?keys?seq_contains("printable")>
  <div class="dload-pdf">
    <a href="<@siteLink handlerName="assetFile" queryParameters=(articlePtr + {"type": "printable"}) />"
       id="downloadPdf" target="_blank">Download PDF</a>
  </div>
<#else>
  <div class="dload-pdf no-pdf">Download</div>
</#if>
  <div data-js-tooltip-hover="trigger" class="dload-hover">&nbsp;
    <ul class="dload-xml" data-js-tooltip-hover="target">
      <li><a href="<@siteLink handlerName="citationDownloadPage" queryParameters={'id': article.doi} />"
             id="downloadCitation">Citation</a></li>
      <li><a href="<@siteLink handlerName="assetFile" queryParameters=(articlePtr + {"type": "manuscript"}) />"
             id="downloadXml">XML</a>
      </li>
    </ul>

  </div>
</div>
